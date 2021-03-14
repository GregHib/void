package world.gregs.voidps.handle

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.client.ui.isOpen
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.setDisplayMode
import world.gregs.voidps.network.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class ScreenChangeHandler : Handler() {

    val sessions: Sessions by inject()

    override fun changeScreen(player: Player, displayMode: Int, width: Int, height: Int, antialiasLevel: Int) {
        delay {
            player.gameFrame.width = width
            player.gameFrame.height = height

            if (player.gameFrame.displayMode == displayMode || !player.isOpen("graphics_options")) {
                return@delay
            }
            player.setDisplayMode(displayMode)
        }
    }

}