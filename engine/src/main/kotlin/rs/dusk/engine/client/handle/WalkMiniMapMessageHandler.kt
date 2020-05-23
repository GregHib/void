package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.session.Sessions
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.WalkMiniMapMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class WalkMiniMapMessageHandler : GameMessageHandler<WalkMiniMapMessage>() {

    val sessions: Sessions by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: WalkMiniMapMessage) {
        val session = ctx.channel().getSession()
        sessions.send(session, msg)
    }

}