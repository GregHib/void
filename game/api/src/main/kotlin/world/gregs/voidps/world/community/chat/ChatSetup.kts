package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.playerSpawn

playerSpawn { player ->
    player.sendVariable("clan_chat_colour")
    player.sendVariable("private_chat_colour")
}

interfaceOption("Open chat display options", "chat", "options") {
    player.open("chat_setup")
}

interfaceOption("No split", "no_split", "chat_setup") {
    player["private_chat_colour"] = -1
}

interfaceOption("Select colour", "clan_colour*", "chat_setup") {
    val index = component.removePrefix("clan_colour").toInt()
    player["clan_chat_colour"] = index - 1
}

interfaceOption("Select colour", "private_colour*", "chat_setup") {
    val index = component.removePrefix("private_colour").toInt()
    player["private_chat_colour"] = index
}

interfaceOption("Close", "close", "chat_setup") {
    player.open("options")
}