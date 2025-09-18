package content.entity.player.modal.tab

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class Options : Api {

    override fun spawn(player: Player) {
        player.sendVariable("accept_aid")
    }

    init {
        interfaceOption("Graphics Settings", "graphics", "options") {
            if (player.hasMenuOpen()) {
                player.message("Please close the interface you have open before setting your graphics options.")
                return@interfaceOption
            }
            player.open("graphics_options")
        }

        interfaceOption("Audio Settings", "audio", "options") {
            if (player.hasMenuOpen()) {
                player.message("Please close the interface you have open before setting your audio options.")
                return@interfaceOption
            }
            player.open("audio_options")
        }

        interfaceOption("Toggle Accept Aid", "aid", "options") {
            player.toggle("accept_aid")
        }
    }
}
