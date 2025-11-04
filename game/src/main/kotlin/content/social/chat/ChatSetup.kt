package content.social.chat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class ChatSetup : Script {

    init {
        playerSpawn {
            sendVariable("clan_chat_colour")
            sendVariable("private_chat_colour")
        }

        interfaceOption("Open chat display options", "options:chat") {
            open("chat_setup")
        }

        interfaceOption("No split", "chat_setup:no_split") {
            set("private_chat_colour", -1)
        }

        interfaceOption("Select colour", "chat_setup:clan_colour*") {
            val index = it.component.removePrefix("clan_colour").toInt()
            set("clan_chat_colour", index - 1)
        }

        interfaceOption("Select colour", "chat_setup:private_colour*") {
            val index = it.component.removePrefix("private_colour").toInt()
            set("private_chat_colour", index)
        }

        interfaceOption("Close", "chat_setup:close") {
            open("options")
        }
    }
}
