package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message

on<InterfaceOption>({ name == "friends_chat" && component == "settings" && option == "Open Settings" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before using Friends Chat setup.")
        return@on
    }
    player.open("friends_chat_setup")
}