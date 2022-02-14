import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.isDoor
import world.gregs.voidps.engine.utility.isGate
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.interact.entity.obj.Door.getRotation
import world.gregs.voidps.world.interact.entity.obj.Door.getTile
import world.gregs.voidps.world.interact.entity.obj.Door.openDoubleDoors
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val objects: Objects by inject()

// Delay in ticks before a door closes itself
val doorResetDelay = TimeUnit.MINUTES.toTicks(5)
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

on<ObjectOption>({ obj.def.isDoor() && option == "Close" }) { player: Player ->
    player.action(ActionType.OpenDoor) {
        // Prevent players from trapping one another
        if (stuck(player)) {
            return@action
        }

        val double = getDoubleDoor(obj, 1)
        if (resetExisting(obj, double)) {
            player.playSound(if (obj.def.isGate()) "close_gate" else "close_door")
            return@action
        }

        if (double == null && obj.id.endsWith("_opened")) {
            obj.replace(
                obj.id.replace("_opened", "_closed"),
                getTile(obj, 0),
                obj.type,
                getRotation(obj, 3),
                doorResetDelay
            )
            player.playSound("close_door")
            return@action
        }

        if (double != null && obj.id.endsWith("_opened") && double.id.endsWith("_opened")) {
            if (obj.def.isGate()) {
                TODO("Not yet implemented.")
            } else {
                val delta = obj.tile.delta(double.tile)
                val dir = Direction.cardinal[obj.rotation]
                val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
                replaceObjectPair(
                    obj,
                    obj.id.replace("_opened", "_closed"),
                    getTile(obj, 0),
                    getRotation(obj, if (flip) 1 else 3),
                    double,
                    double.id.replace("_opened", "_closed"),
                    getTile(double, 2),
                    getRotation(double, if (flip) 3 else 1),
                    doorResetDelay
                )
                player.playSound("close_door")
            }
            return@action
        }
        player.message("The ${obj.def.name.lowercase()} won't budge.")
    }
}

on<ObjectOption>({ obj.def.isDoor() && option == "Open" }) { player: Player ->
    player.action(ActionType.OpenDoor) {
        val double = getDoubleDoor(obj, 0)

        if (resetExisting(obj, double)) {
            player.playSound(if (obj.def.isGate()) "open_gate" else "open_door")
            return@action
        }

        if (double == null && obj.id.endsWith("_closed")) { // Single Doors
            obj.replace(
                obj.id.replace("_closed", "_opened"),
                getTile(obj, 1),
                obj.type,
                getRotation(obj, 1),
                doorResetDelay
            )
            player.playSound("open_door")
            delay(1)
            return@action
        }
        if (double != null && obj.id.endsWith("_closed") && double.id.endsWith("_closed")) {
            openDoubleDoors(obj, double, doorResetDelay)
            player.playSound("open_door")
            delay(1)
            return@action
        }
        player.message("The ${obj.def.name.lowercase()} won't budge.")
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