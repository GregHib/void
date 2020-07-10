package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.WalkMapMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class WalkMapMessageHandler : GameMessageHandler<WalkMapMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: WalkMapMessage) {
        val session = ctx.channel().getSession()
        sessions.send(session, msg)
    }

}