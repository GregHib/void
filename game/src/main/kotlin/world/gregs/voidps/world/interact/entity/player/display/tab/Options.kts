package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOption({ id == "options" && component == "graphics" && option == "Graphics Settings" }) { player: Player ->
    if (player.hasMenuOpen()) {
        player.message("Please close the interface you have open before setting your graphics options.")
        return@interfaceOption
    }
    player.open("graphics_options")
}

interfaceOption({ id == "options" && component == "audio" && option == "Audio Settings" }) { player: Player ->
    if (player.hasMenuOpen()) {
        player.message("Please close the interface you have open before setting your audio options.")
        return@interfaceOption
    }
    player.open("audio_options")
}