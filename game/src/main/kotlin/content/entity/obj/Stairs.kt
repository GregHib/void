package content.entity.obj

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Teleport

class Stairs(val teleports: ObjectTeleports) : Script {

    init {
        objectOperate("Climb", arrive = false) { (target) ->
            if (target.def(this).options?.filterNotNull()?.any { it.startsWith("Climb-") } != true) {
                return@objectOperate
            }
            choice("What would you like to do?") {
                option("Go up the stairs.", block = { teleports.teleport(this, target, "Climb-up") })
                option("Go down the stairs.", block = { teleports.teleport(this, target, "Climb-down") })
                option("Never mind.")
            }
        }

        objTeleportTakeOff { target, option ->
            val obj = target.def(this)
            if (!obj.name.isLadder()) {
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            val remaining = remaining("teleport_delay")
            if (remaining > 0) {
                return@objTeleportTakeOff remaining
            } else if (remaining < 0) {
                anim(if (option == "Climb-down" || obj.stringId.endsWith("_down")) "climb_down" else "climb_up")
                start("teleport_delay", 2)
                return@objTeleportTakeOff 2
            }
            return@objTeleportTakeOff Teleport.CONTINUE
        }
    }

    fun String.isLadder() = contains("ladder", true) || contains("rope", true) || contains("chain", true) || contains("vine", true) || isTrapDoor()

    fun String.isTrapDoor(): Boolean {
        val name = replace(" ", "")
        return name.equals("trapdoor", true) || name.equals("manhole", true)
    }
}
