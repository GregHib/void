import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.isDoor
import world.gregs.voidps.engine.utility.isGate
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val objects: Objects by inject()
val definitions: ObjectDefinitions by inject()

// Delay in ticks before a door closes itself
val doorResetDelay = TimeUnit.MINUTES.toTicks(5)
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

on<ObjectOption>({ obj.def.isDoor() && option == "Close" }) { player: Player ->
    // Prevent players from trapping one another
    if (stuck(player)) {
        return@on
    }

    val double = getDoubleDoor(obj, 1)
    if (resetExisting(obj, double)) {
        player.playSound(if (obj.def.isGate()) "close_gate" else "close_door")
        return@on
    }

    val replacement1 = obj.def.getOrNull("close") as? Int
    if (double == null && replacement1 != null) {
        obj.replace(
            definitions.get(replacement1).stringId,
            getTile(obj, 0),
            obj.type,
            getRotation(obj, 3),
            doorResetDelay
        )
        player.playSound("close_door")
        return@on
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
                definitions.get(replacement1).stringId,
                getTile(obj, 0),
                getRotation(obj, if (flip) 1 else 3),
                double,
                definitions.get(replacement2).stringId,
                getTile(double, 2),
                getRotation(double, if (flip) 3 else 1),
                10
            )
            player.playSound("close_door")
        }
        return@on
    }
    player.message("The ${obj.def.name.toLowerCase()} won't budge.")
}

on<ObjectOption>({ obj.def.isDoor() && option == "Open" }) { player: Player ->
    player.action(ActionType.OpenDoor) {
        val double = getDoubleDoor(obj, 0)

        if (resetExisting(obj, double)) {
            player.playSound(if (obj.def.isGate()) "open_gate" else "open_door")
            return@action
        }

        val replacement1 = obj.def.getOrNull("open") as? Int
        val replacement2 = double?.def?.getOrNull("open") as? Int

        if (double == null && replacement1 != null) {// Single Doors
            obj.replace(
                definitions.get(replacement1).stringId,
                getTile(obj, 1),
                obj.type,
                getRotation(obj, 1),
                doorResetDelay
            )
            player.playSound("open_door")
            delay(1)
            return@action
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
                player.playSound("open_gate")
            } else {// Double doors
                replaceObjectPair(
                    obj,
                    definitions.get(replacement1).stringId,
                    getTile(obj, 1),
                    getRotation(obj, if (flip) 1 else 3),
                    double,
                    definitions.get(replacement2).stringId,
                    getTile(double, 1),
                    getRotation(double, if (flip) 3 else 1),
                    doorResetDelay
                )
                player.playSound("open_door")
            }
            delay(1)
            return@action
        }
        player.message("The ${obj.def.name.toLowerCase()} won't budge.")
    }
}

fun stuck(player: Player): Boolean {
    if (player.hasEffect("stuck_door")) {
        player.message("The door seems to be stuck.")
        return true
    }
    if (player.hasEffect("recently_opened_door")) {
        if (player.inc("door_slam_count") >= doorStuckCount) {
            player.start("stuck_door", TimeUnit.MINUTES.toTicks(1))
            return true
        }
    } else {
        player.clear("door_slam_count")
    }
    player.start("recently_opened_door", 10)
    return false
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