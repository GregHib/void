package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<Registered> { player: Player ->
    player.sendVariable("clan_chat_colour")
    player.sendVariable("private_chat_colour")
}

on<InterfaceOption>({ id == "options" && component == "chat" && option == "Open chat display options" }) { player: Player ->
    player.open("chat_setup")
}

on<InterfaceOption>({ id == "chat_setup" && component == "no_split" && option == "No split" }) { player: Player ->
    player["private_chat_colour"] = -1
}

on<InterfaceOption>({ id == "chat_setup" && component.startsWith("clan_colour") && option == "Select colour" }) { player: Player ->
    val index = component.removePrefix("clan_colour").toInt()
    player["clan_chat_colour"] = index - 1
}

on<InterfaceOption>({ id == "chat_setup" && component.startsWith("private_colour") && option == "Select colour" }) { player: Player ->
    val index = component.removePrefix("private_colour").toInt()
    player["private_chat_colour"] = index
}

on<InterfaceOption>({ id == "chat_setup" && component == "close" && option == "Close" }) { player: Player ->
    player.open("options")
}