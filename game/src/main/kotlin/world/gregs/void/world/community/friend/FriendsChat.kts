package world.gregs.void.world.community.friend

import world.gregs.void.engine.client.ui.hasOpen
import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.event.on
import world.gregs.void.engine.event.then
import world.gregs.void.network.codec.game.encode.message
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

on(InterfaceOption) {
    where {
        name == "friends_chat" && component == "settings" && option == "Open Settings"
    }
    then {
        if(player.hasOpen("main_screen")) {
            player.message("Please close the interface you have open before using Friends Chat setup.")
            return@then
        }
        player.open("friends_chat_setup")
    }
}