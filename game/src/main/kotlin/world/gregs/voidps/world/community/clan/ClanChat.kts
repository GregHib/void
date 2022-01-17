package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "clan_chat" && component == "settings" && option == "Clan Setup" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before using Clan world.gregs.voidps.world.community.chat.Chat setup.")
        return@on
    }
    player.open("clan_chat_setup")
}