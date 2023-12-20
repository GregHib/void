package world.gregs.voidps.world.interact.entity.obj.door

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.obj.door.Door.closeDoor
import world.gregs.voidps.world.interact.entity.obj.door.Door.isDoor
import world.gregs.voidps.world.interact.entity.obj.door.Door.openDoor

// Times a door can be closed consecutively before getting stuck
val doorStuckCount = 5

on<ObjectOption>({ operate && def.isDoor() && option == "Close" }) { player: Player ->
    arriveDelay()
    // Prevent players from trapping one another
    if (stuck(player)) {
        return@on
    }
    closeDoor(player, target, def)
}

on<ObjectOption>({ operate && def.isDoor() && option == "Open" }) { player: Player ->
    arriveDelay()
    if (openDoor(player, target, def)) {
        pause(1)
        player.events.emit(DoorOpened)
    }
}

fun stuck(player: Player): Boolean {
    if (player.remaining("stuck_door", epochSeconds()) > 0) {
        player.message("The door seems to be stuck.")
        return true
    }
    if (player.hasClock("recently_opened_door")) {
        if (player.inc("door_slam_count") >= doorStuckCount) {
            player.start("stuck_door", 60, epochSeconds())
            return true
        }
    } else {
        player.clear("door_slam_count")
    }
    player.start("recently_opened_door", 10)
    return false
}