import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.character.clear
import rs.dusk.engine.model.entity.character.inc
import rs.dusk.engine.model.entity.character.player.chat.message
import rs.dusk.engine.model.entity.character.player.delay.Delay
import rs.dusk.engine.model.entity.character.player.delay.isDelayed
import rs.dusk.engine.model.entity.character.player.delay.start
import rs.dusk.engine.model.entity.obj.GameObject
import rs.dusk.engine.model.entity.obj.ObjectOption
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.replaceObject
import rs.dusk.world.entity.obj.replaceObjectPair

val objects: Objects by inject()
val loader: FileLoader by inject()
val logger = InlineLogger()

// Delay in ticks before a door closes itself
val doorCloseDelay = 500
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

val doors: Map<Int, Int> = loader.load<Map<String, Int>>(getProperty("doorsPath")).mapKeys { it.key.toInt() }
val fences: Map<Int, Int> = loader.load<Map<String, Int>>(getProperty("fencesPath")).mapKeys { it.key.toInt() }

fun GameObject.isDoor() = def.name.contains("door", true) || def.name.contains("gate", true)

ObjectOption where { obj.isDoor() && option == "Close" } then {
    // Prevent players from trapping one another
    if(player.isDelayed(Delay.DoorSlam)) {
        if(player.inc("doorSlamCount") > doorStuckCount) {
            player.message("The door seems to be stuck.")
            return@then
        }
    } else {
        player.clear("doorSlamCount")
    }
    player.start(Delay.DoorSlam)

    // Close door
    val double = getDoubleDoor(obj, 1)
    if (double == null) {
        if (!objects.cancelTimer(obj)) {
            logger.warn { "Unknown door ${option?.toLowerCase()} $obj" }
        }
    } else {
        if (!objects.cancelTimer(obj) || !objects.cancelTimer(double)) {
            logger.warn { "Unknown fence/double door ${option?.toLowerCase()} $obj" }
        }
    }
}

ObjectOption where { obj.isDoor() && option == "Open" } then {
    val double = getDoubleDoor(obj, 0)

    val replacement1 = doors[obj.id]
    val replacement3 = fences[obj.id]
    if (double != null) {
        val replacement2 = doors[double.id]
        val replacement4 = fences[double.id]

        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (replacement1 != null && replacement2 != null) {// Double doors
            replaceObjectPair(
                obj,
                replacement1,
                getTile(obj, 1),
                getRotation(obj, if (flip) 1 else 3),
                double,
                replacement2,
                getTile(double, 1),
                getRotation(double, if (flip) 3 else 1),
                doorCloseDelay
            )
        } else if (replacement3 != null && replacement4 != null) {// Fences
            val first = if (flip) double else obj
            val second = if (flip) obj else double
            val tile = getTile(first, 1)
            replaceObjectPair(
                first,
                fences.getValue(first.id),
                tile,
                getRotation(first, 3),
                second,
                fences.getValue(second.id),
                getTile(tile, second.rotation, 1),
                getRotation(second, 3),
                doorCloseDelay
            )
        }
    } else if (replacement1 != null) {// Single Doors
        replaceObject(
            obj,
            replacement1,
            getTile(obj, 1),
            obj.type,
            getRotation(obj, 1),
            doorCloseDelay
        )
    }
}

fun getTile(gameObject: GameObject, anticlockwise: Int) = getTile(gameObject.tile, gameObject.rotation, anticlockwise)

fun getTile(tile: Tile, rotation: Int, anticlockwise: Int): Tile {
    val orientation = Direction.cardinal[getRotation(rotation, -anticlockwise)]
    return tile.add(orientation.delta)
}

fun getRotation(gameObject: GameObject, clockwise: Int) = getRotation(gameObject.rotation, clockwise)

fun getRotation(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3

fun getDoubleDoor(gameObject: GameObject, clockwise: Int): GameObject? {
    var orientation = Direction.cardinal[getRotation(gameObject, clockwise)]
    var door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
    if (door != null && door.isDoor()) {
        return door
    }
    orientation = orientation.inverse()
    door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
    if (door != null && door.isDoor()) {
        return door
    }
    return null
}