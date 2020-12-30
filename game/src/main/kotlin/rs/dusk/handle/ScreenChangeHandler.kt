package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.isOpen
import rs.dusk.engine.entity.character.player.setDisplayMode
import rs.dusk.network.codec.Handler
import rs.dusk.utility.inject

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