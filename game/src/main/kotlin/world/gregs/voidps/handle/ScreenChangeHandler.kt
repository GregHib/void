package world.gregs.voidps.handle

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.client.ui.isOpen
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.setDisplayMode
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class ScreenChangeHandler : Handler() {

    val sessions: Sessions by inject()

    override fun changeScreen(session: ClientSession, displayMode: Int, width: Int, height: Int, antialiasLevel: Int) {
        val player = sessions.get(session) ?: return
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