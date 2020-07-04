import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.ObjectOption
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.ReplaceObject
import rs.dusk.world.entity.obj.ReplaceObjectPair

val objects: Objects by inject()
val loader: FileLoader by inject()
val bus: EventBus by inject()
val logger = InlineLogger()

val doorCloseDelay = 500

val doors: Map<Int, Int> = loader.load<Map<String, Int>>("./cache/data/doors.yml")!!.mapKeys { it.key.toInt() }
val fences: Map<Int, Int> = loader.load<Map<String, Int>>("./cache/data/fences.yml")!!.mapKeys { it.key.toInt() }

fun Location.isDoor() = def.name.contains("door", true) || def.name.contains("gate", true)

ObjectOption where { gameObject.isDoor() && option == "Close" } then {
    val double = getDoubleDoor(gameObject, 1)
    if (double == null) {
        if (!objects.cancelTimer(gameObject)) {
            logger.warn { "Unknown door ${option?.toLowerCase()} $gameObject" }
        }
    } else {
        if (!objects.cancelTimer(gameObject) || !objects.cancelTimer(double)) {
            logger.warn { "Unknown fence/double door ${option?.toLowerCase()} $gameObject" }
        }
    }

}

ObjectOption where { gameObject.isDoor() && option == "Open" } then {
    val double = getDoubleDoor(gameObject, 0)

    val replacement1 = doors[gameObject.id]
    val replacement3 = fences[gameObject.id]
    if (double != null) {
        val replacement2 = doors[double.id]
        val replacement4 = fences[double.id]

        val delta = gameObject.tile.delta(double.tile)
        val dir = Direction.cardinal[gameObject.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (replacement1 != null && replacement2 != null) {// Double doors
            bus.emit(
                ReplaceObjectPair(
                    gameObject,
                    replacement1,
                    getTile(gameObject, 1),
                    getRotation(gameObject, if (flip) 1 else 3),
                    double,
                    replacement2,
                    getTile(double, 1),
                    getRotation(double, if (flip) 3 else 1),
                    doorCloseDelay
                )
            )
        } else if (replacement3 != null && replacement4 != null) {// Fences
            val first = if(flip) double else gameObject
            val second = if(flip) gameObject else double
            val tile = getTile(first, 1)
            bus.emit(
                ReplaceObjectPair(
                    first,
                    fences.getValue(first.id),
                    tile,
                    getRotation(first, 3),
                    second,
                    fences.getValue(second.id),
                    getTile(tile, second.rotation, 1),
                    getRotation(double, 3),
                    doorCloseDelay
                )
            )
        }
    } else if (replacement1 != null) {// Single Doors
        bus.emit(
            ReplaceObject(
                gameObject,
                replacement1,
                getTile(gameObject, 1),
                gameObject.type,
                getRotation(gameObject, 1),
                doorCloseDelay
            )
        )
    }
}

fun getTile(tile: Tile, rotation: Int, anticlockwise: Int): Tile {
    val orientation = Direction.cardinal[rotation - anticlockwise and 0x3]
    return tile.add(orientation.delta)
}

fun getTile(location: Location, anticlockwise: Int): Tile {
    val orientation = Direction.cardinal[location.rotation - anticlockwise and 0x3]
    return location.tile.add(orientation.delta)
}

fun getRotation(location: Location, clockwise: Int): Int {
    return (location.rotation + clockwise) and 0x3
}

fun getDoubleDoor(location: Location, clockwise: Int): Location? {
    var orientation = Direction.cardinal[location.rotation + clockwise and 0x3]
    var door = objects.getType(location.tile.add(orientation.delta), location.type)
    if (door != null && door.isDoor()) {
        return door
    }
    orientation = orientation.inverse()
    door = objects.getType(location.tile.add(orientation.delta), location.type)
    if (door != null && door.isDoor()) {
        return door
    }
    return null
}