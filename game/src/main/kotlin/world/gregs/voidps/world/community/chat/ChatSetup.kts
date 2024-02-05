package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn

playerSpawn { player: Player ->
    player.sendVariable("clan_chat_colour")
    player.sendVariable("private_chat_colour")
}

interfaceOption("options", "chat", "Open chat display options") {
    player.open("chat_setup")
}

interfaceOption("chat_setup", "no_split", "No split") {
    player["private_chat_colour"] = -1
}

interfaceOption("chat_setup", "clan_colour*", "Select colour") {
    val index = component.removePrefix("clan_colour").toInt()
    player["clan_chat_colour"] = index - 1
}

interfaceOption("chat_setup", "private_colour*", "Select colour") {
    val index = component.removePrefix("private_colour").toInt()
    player["private_chat_colour"] = index
}

interfaceOption("chat_setup", "close", "Close") {
    player.open("options")
}