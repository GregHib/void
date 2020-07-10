package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ScreenChangeMessage
import rs.dusk.network.rs.codec.game.encode.message.WindowUpdateMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class ScreenChangeMessageHandler : GameMessageHandler<ScreenChangeMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ScreenChangeMessage) {
//        val session = ctx.channel().getSession()
//        sessions.send(session, msg)
        ctx.channel().pipeline().writeAndFlush(WindowUpdateMessage(746, 0))
    }

}