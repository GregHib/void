package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

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