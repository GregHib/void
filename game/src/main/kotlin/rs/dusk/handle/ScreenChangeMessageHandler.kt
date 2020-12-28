package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.client.ui.isOpen
import rs.dusk.engine.entity.character.player.setDisplayMode
import rs.dusk.network.rs.codec.game.decode.message.ScreenChangeMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class ScreenChangeMessageHandler : MessageHandler<ScreenChangeMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ScreenChangeMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (mode, width, height, antialias) = msg
        player.gameFrame.width = width
        player.gameFrame.height = height

        if(player.gameFrame.displayMode == mode || !player.isOpen("graphics_options")) {
            return
        }
        player.setDisplayMode(mode)
    }

}