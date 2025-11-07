package content.entity.player.dialogue.type

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.StringSuspension

suspend fun Player.warning(id: String): Boolean {
    val count = get("warning_$id", 0)
    if (count == 7) {
        return true
    }
    check(open("warning_$id")) { "Unable to open warning dialogue warning_$id for $this" }
    interfaces.sendVisibility("warning_$id", "ask_again", count == 6)
    val result = StringSuspension.get(this) == "yes"
    close("warning_$id")
    return result
}

class Warning : Script {

    init {
        interfaceOption("Yes", "warning_*:yes") {
            (dialogueSuspension as StringSuspension).resume("yes")
        }

        interfaceOption("No", "warning_*:no") {
            (dialogueSuspension as StringSuspension).resume("no")
        }

        interfaceOption("Off/On", "warning_*:dont_ask") {
            val id = it.id.substringBefore(":")
            val count = get(id, 0)
            if (count == 6) {
                set(id, 7)
            } else {
                set(id, 6)
            }
        }
    }
}
