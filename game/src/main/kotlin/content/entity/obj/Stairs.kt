package content.entity.obj

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject

class Stairs : Script {

    val teleports: ObjectTeleports by inject()

    init {
        objectOperate("Climb", arrive = false) {
            if (def.options?.filterNotNull()?.any { it.startsWith("Climb-") } != true) {
                return@objectOperate
            }
            choice("What would you like to do?") {
                option("Go up the stairs.", block = { teleports.teleport(this, "Climb-up") })
                option("Go down the stairs.", block = { teleports.teleport(this, "Climb-down") })
                option("Never mind.")
            }
        }

        objTeleportTakeOff {
            if (!obj.name.isLadder()) {
                return@objTeleportTakeOff
            }
            val remaining = player.remaining("teleport_delay")
            if (remaining > 0) {
                delay = remaining
            } else if (remaining < 0) {
                player.anim(if (option == "Climb-down" || obj.stringId.endsWith("_down")) "climb_down" else "climb_up")
                player.start("teleport_delay", 2)
                delay = 2
            }
        }
    }

    fun String.isLadder() = contains("ladder", true) || contains("rope", true) || contains("chain", true) || contains("vine", true) || isTrapDoor()

    fun String.isTrapDoor(): Boolean {
        val name = replace(" ", "")
        return name.equals("trapdoor", true) || name.equals("manhole", true)
    }
}
