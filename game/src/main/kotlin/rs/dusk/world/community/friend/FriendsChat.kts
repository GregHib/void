package rs.dusk.world.community.friend

import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.hasOpen
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.character.player.chat.message

on(InterfaceInteraction) {
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