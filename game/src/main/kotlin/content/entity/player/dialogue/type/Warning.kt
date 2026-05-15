package content.entity.player.dialogue.type

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseString

suspend fun Player.warning(id: String): Boolean {
    val count = get("warning_$id", 0)
    if (count == 7) {
        return true
    }
    if (!open("warning_$id")) {
        return false
    }
    interfaces.sendVisibility("warning_$id", "ask_again", count == 6)
    val result = pauseString() == "yes"
    close("warning_$id")
    return result
}

class Warning : Script {

    init {
        interfaceOption("Yes", "warning_*:yes") {
            info(it.id)
            (suspension as Suspension.StringEntry).resume("yes")
        }

        interfaceOption("Ok", "warning_*:yes") {
            info(it.id)
            (suspension as Suspension.StringEntry).resume("yes")
        }

        interfaceOption("No", "warning_*:no") {
            (suspension as Suspension.StringEntry).resume("no")
        }

        continueDialogue("warning_*:yes") {
            info(it)
            (suspension as Suspension.StringEntry).resume("yes")
        }

        continueDialogue("warning_*:no") {
            (suspension as Suspension.StringEntry).resume("no")
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

    private fun Player.info(id: String) {
        if (get(id, 0) == 7) {
            // https://youtu.be/6j15c74a3uM?t=76
            message("You have toggled this warning screen off. You will not see this warning screen unless you speak to the Doomsayer in Lumbridge to turn it on again.")
        }
    }
}
