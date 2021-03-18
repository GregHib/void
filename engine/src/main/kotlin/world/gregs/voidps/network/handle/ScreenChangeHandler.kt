package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.client.ui.isOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.setDisplayMode
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class ScreenChangeHandler : Handler() {

    override fun changeScreen(player: Player, displayMode: Int, width: Int, height: Int, antialiasLevel: Int) {
        if (player.gameFrame.displayMode == displayMode || !player.isOpen("graphics_options")) {
            return
        }
        player.setDisplayMode(displayMode)
    }

}