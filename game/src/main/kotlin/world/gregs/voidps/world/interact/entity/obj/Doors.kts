import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.inc
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.delayForever
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.isDoor
import world.gregs.voidps.engine.utility.isGate
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.interact.entity.obj.Door.closeDoubleDoors
import world.gregs.voidps.world.interact.entity.obj.Door.getDoubleDoor
import world.gregs.voidps.world.interact.entity.obj.Door.openDoubleDoors
import world.gregs.voidps.world.interact.entity.obj.Door.replaceDoor
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val objects: Objects by inject()

// Delay in ticks before a door closes itself
val doorResetDelay = TimeUnit.MINUTES.toTicks(5)
// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

on<ObjectOption>({ def.isDoor() && option == "Close" }) { player: Player ->
    player.action(ActionType.OpenDoor) {
        // Prevent players from trapping one another
        if (stuck(player)) {
            return@action
        }

        val double = getDoubleDoor(objects, obj, def, 1)
        if (resetExisting(obj, double)) {
            player.playSound(if (def.isGate()) "close_gate" else "close_door")
            return@action
        }

        // Single door
        if (double == null && obj.id.endsWith("_opened")) {
            replaceDoor(obj, def, "_opened", "_closed", 0, 3, doorResetDelay)
            player.playSound("close_door")
            return@action
        }

        // Double doors
        if (double != null && obj.id.endsWith("_opened") && double.id.endsWith("_opened")) {
            closeDoubleDoors(obj, def, double, doorResetDelay)
            player.playSound("close_door")
            return@action
        }
        player.message("The ${def.name.lowercase()} won't budge.")
    }
    delayForever()
}

on<ObjectOption>({ def.isDoor() && option == "Open" }) { player: Player ->
    player.action(ActionType.OpenDoor) {
        val double = getDoubleDoor(objects, obj, def, 0)

        if (resetExisting(obj, double)) {
            player.playSound(if (def.isGate()) "open_gate" else "open_door")
            return@action
        }

        // Single door
        if (double == null && obj.id.endsWith("_closed")) {
            replaceDoor(obj, def, "_closed", "_opened", 1, 1, doorResetDelay)
            player.playSound("open_door")
            delay(1)
            return@action
        }

        // Double doors
        if (double != null && obj.id.endsWith("_closed") && double.id.endsWith("_closed")) {
            openDoubleDoors(obj, def, double, doorResetDelay)
            player.playSound("open_door")
            delay(1)
            return@action
        }
        player.message("The ${def.name.lowercase()} won't budge.")
    }
    delayForever()
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