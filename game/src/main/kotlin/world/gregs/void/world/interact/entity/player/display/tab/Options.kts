package world.gregs.void.world.interact.entity.player.display.tab

import world.gregs.void.engine.client.ui.hasOpen
import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.event.on
import world.gregs.void.engine.event.then
import world.gregs.void.network.codec.game.encode.message
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

on(InterfaceOption) {
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

on(InterfaceOption) {
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