import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.clear
import world.gregs.voidps.engine.entity.character.inc
import world.gregs.voidps.engine.entity.character.player.delay.Delay
import world.gregs.voidps.engine.entity.character.player.delay.delayed
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.func.isDoor
import world.gregs.voidps.utility.func.isGate
import world.gregs.voidps.utility.inject

val objects: Objects by inject()
val logger = InlineLogger()

// Delay in ticks before a door closes itself
val doorResetDelay = 500
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

ObjectOption where { obj.def.isDoor() && option == "Close" } then {
    // Prevent players from trapping one another
    if (player.delayed(Delay.DoorSlam)) {
        if (player.inc("doorSlamCount") > doorStuckCount) {
            player.message("The door seems to be stuck.")
            return@then
        }
    } else {
        player.clear("doorSlamCount")
    }

    val double = getDoubleDoor(obj, 1)

    if (resetExisting(obj, double)) {
        return@then
    }

    val replacement1 = obj.def.getOrNull("close") as? Int

    if (double == null && replacement1 != null) {
        obj.replace(
            replacement1,
            getTile(obj, 0),
            obj.type,
            getRotation(obj, 3),
            doorResetDelay
        )
        return@then
    }

    val replacement2 = double?.def?.getOrNull("close") as? Int
    if (double != null && replacement1 != null && replacement2 != null) {
        if (obj.def.isGate()) {
            TODO("Not yet implemented.")
        } else {
            val delta = obj.tile.delta(double.tile)
            val dir = Direction.cardinal[obj.rotation]
            val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
            replaceObjectPair(
                obj,
                replacement1,
                getTile(obj, 0),
                getRotation(obj, if (flip) 1 else 3),
                double,
                replacement2,
                getTile(double, 2),
                getRotation(double, if (flip) 3 else 1),
                10
            )
        }
        return@then
    }
    player.message("The ${obj.def.name.toLowerCase()} won't budge.")
}

ObjectOption where { obj.def.isDoor() && option == "Open" } then {
    val double = getDoubleDoor(obj, 0)

    if (resetExisting(obj, double)) {
        return@then
    }

    val replacement1 = obj.def.getOrNull("open") as? Int
    val replacement2 = double?.def?.getOrNull("open") as? Int

    if (double == null && replacement1 != null) {// Single Doors
        obj.replace(
            replacement1,
            getTile(obj, 1),
            obj.type,
            getRotation(obj, 1),
            doorResetDelay
        )
        return@then
    }
    if (double != null && replacement1 != null && replacement2 != null) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (obj.def.isGate()) {
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
                doorResetDelay
            )
        } else {// Double doors
            replaceObjectPair(
                obj,
                replacement1,
                getTile(obj, 1),
                getRotation(obj, if (flip) 1 else 3),
                double,
                replacement2,
                getTile(double, 1),
                getRotation(double, if (flip) 3 else 1),
                doorResetDelay
            )
        }
        return@then
    }
    player.message("The ${obj.def.name.toLowerCase()} won't budge.")
}

fun resetExisting(obj: GameObject, double: GameObject?): Boolean {
    if (double == null && objects.cancelTimer(obj)) {
        return true
    }

    if (double != null && objects.cancelTimer(obj) && objects.cancelTimer(double)) {
        return true
    }
    return false
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