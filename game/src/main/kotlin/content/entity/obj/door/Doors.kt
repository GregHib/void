package content.entity.obj.door

import content.entity.obj.door.Door.closeDoor
import content.entity.obj.door.Door.isDoor
import content.entity.obj.door.Door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.epochSeconds

class Doors : Script {

    val doorStuckCount = 5

    init {
        objectOperate("Close") { (target) ->
            val def = target.def(this)
            if (!def.isDoor()) {
                return@objectOperate
            }
            // Prevent players from trapping one another
            if (stuck(this)) {
                return@objectOperate
            }
            closeDoor(this, target, def)
        }

        objectOperate("Open") { (target) ->
            val def = target.def(this)
            if (!def.isDoor()) {
                return@objectOperate
            }
            if (openDoor(this, target, def)) {
                delay(0)
                Door.opened?.invoke(this)
            }
        }
    }

    // Times a door can be closed consecutively before getting stuck

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
}
