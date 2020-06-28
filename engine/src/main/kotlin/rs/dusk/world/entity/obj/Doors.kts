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

ObjectOption where { location.isDoor() && option == "Close" } then {
    val double = getDoubleDoor(location, 1)
    if (double == null) {
        if (!objects.cancelTimer(location)) {
            logger.warn { "Unknown door ${option?.toLowerCase()} $location" }
        }
    } else {
        if (!objects.cancelTimer(location) || !objects.cancelTimer(double)) {
            logger.warn { "Unknown fence/double door ${option?.toLowerCase()} $location" }
        }
    }

}

ObjectOption where { location.isDoor() && option == "Open" } then {
    val double = getDoubleDoor(location, 0)

    val replacement1 = doors[location.id]
    val replacement3 = fences[location.id]
    if (double != null) {
        val replacement2 = doors[double.id]
        val replacement4 = fences[double.id]

        val delta = location.tile.delta(double.tile)
        val dir = Direction.cardinal[location.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (replacement1 != null && replacement2 != null) {
            bus.emit(
                ReplaceObjectPair(
                    location,
                    replacement1,
                    getTile(location, 1),
                    getRotation(location, if (flip) 1 else 3),
                    double,
                    replacement2,
                    getTile(double, 1),
                    getRotation(double, if (flip) 3 else 1),
                    doorCloseDelay
                )
            )
        } else if (replacement3 != null && replacement4 != null) {
            bus.emit(
                ReplaceObjectPair(
                    location,
                    replacement3,
                    getTile(location, if (flip) 3 else 0),
                    getRotation(location, 1),
                    double,
                    replacement4,
                    getTile(double, if (flip) 0 else 3),
                    getRotation(double, 1),
                    doorCloseDelay
                )
            )
        }
    } else if (replacement1 != null) {
        bus.emit(
            ReplaceObject(
                location,
                replacement1,
                getTile(location, 1),
                location.type,
                getRotation(location, 1),
                doorCloseDelay
            )
        )
    }
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