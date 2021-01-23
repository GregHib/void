import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.clear
import world.gregs.voidps.engine.entity.character.inc
import world.gregs.voidps.engine.entity.character.player.delay.Delay
import world.gregs.voidps.engine.entity.character.player.delay.delayed
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.func.isDoor
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.obj.replaceObject
import world.gregs.voidps.world.interact.entity.obj.replaceObjectPair

val objects: Objects by inject()
val logger = InlineLogger()

// Delay in ticks before a door closes itself
val doorCloseDelay = 500
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

ObjectOption where { obj.def.isDoor() && option == "Close" } then {
    // Prevent players from trapping one another
    if(player.delayed(Delay.DoorSlam)) {
        if(player.inc("doorSlamCount") > doorStuckCount) {
            player.message("The door seems to be stuck.")
            return@then
        }
    } else {
        player.clear("doorSlamCount")
    }

    // Close door
    val double = getDoubleDoor(obj, 1)
    if (double == null) {
        if (!objects.cancelTimer(obj)) {
            logger.debug { "Unknown door ${option?.toLowerCase()} $obj" }
        }
    } else {
        if (!objects.cancelTimer(obj) || !objects.cancelTimer(double)) {
            logger.debug { "Unknown fence/double door ${option?.toLowerCase()} $obj" }
        }
    }
}

ObjectOption where { obj.def.isDoor() && option == "Open" } then {
    val double = getDoubleDoor(obj, 0)

    val replacement1 = obj.def.getOrNull("open") as? Int
    val replacement3 = obj.def.getOrNull("open") as? Int
    if (double != null) {
        val replacement2 = double.def.getOrNull("open") as? Int
        val replacement4 = double.def.getOrNull("open") as? Int

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
                first.def["open"],
                tile,
                getRotation(first, 3),
                second,
                second.def["open"],
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
    if (door != null && door.def.isDoor()) {
        return door
    }
    orientation = orientation.inverse()
    door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
    if (door != null && door.def.isDoor()) {
        return door
    }
    return null
}