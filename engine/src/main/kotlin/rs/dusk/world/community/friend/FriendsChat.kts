package rs.dusk.world.community.friend

import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then

on(InterfaceInteraction) {
    where {
        name == "friends_chat" && component == "settings" && option == "Open Settings"
    }
    then {
        player.open("friends_chat_setup")
    }
}