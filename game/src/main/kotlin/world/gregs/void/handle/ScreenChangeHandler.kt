package world.gregs.void.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.engine.client.ui.isOpen
import world.gregs.void.engine.entity.character.player.setDisplayMode
import world.gregs.void.network.codec.Handler
import world.gregs.void.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class ScreenChangeHandler : Handler() {

    val sessions: Sessions by inject()

    override fun changeScreen(context: ChannelHandlerContext, displayMode: Int, width: Int, height: Int, antialiasLevel: Int) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        player.gameFrame.width = width
        player.gameFrame.height = height

        if (player.gameFrame.displayMode == displayMode || !player.isOpen("graphics_options")) {
            return
        }
        player.setDisplayMode(displayMode)
    }

}