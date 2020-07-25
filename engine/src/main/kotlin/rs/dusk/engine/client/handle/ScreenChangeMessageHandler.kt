package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.isOpen
import rs.dusk.engine.client.ui.setDisplayMode
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ScreenChangeMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class ScreenChangeMessageHandler : GameMessageHandler<ScreenChangeMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ScreenChangeMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (mode, width, height, antialias) = msg
        player.gameframe.width = width
        player.gameframe.height = height

        if(player.gameframe.displayMode == mode || !player.isOpen("graphics_options")) {
            return
        }
        player.setDisplayMode(mode)
    }

}