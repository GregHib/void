package rs.dusk.world.entity.player.ui.tab

import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.hasOpen
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.character.player.chat.message

on(InterfaceInteraction) {
    where {
        name == "options" && component == "graphics" && option == "Graphics Settings"
    }
    then {
        if(player.hasOpen("main_screen")) {
            player.message("Please close the interface you have open before setting your graphics options.")
            return@then
        }
        player.open("graphics_options")
    }
}

on(InterfaceInteraction) {
    where {
        name == "options" && component == "audio" && option == "Audio Settings"
    }
    then {
        if(player.hasOpen("main_screen")) {
            player.message("Please close the interface you have open before setting your audio options.")
            return@then
        }
        player.open("audio_options")
    }
}