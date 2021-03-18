package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message

InterfaceOption where { name == "options" && component == "graphics" && option == "Graphics Settings" } then {
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before setting your graphics options.")
        return@then
    }
    player.open("graphics_options")
}

InterfaceOption where { name == "options" && component == "audio" && option == "Audio Settings" } then {
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before setting your audio options.")
        return@then
    }
    player.open("audio_options")
}