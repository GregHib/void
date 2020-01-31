package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.redrune.network.session.Session

class NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = InlineLogger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "Channel connected: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        ctx.channel().getSession().messageReceived(msg)
    }

}

/**
 * Gets the object in the [Session.SESSION_KEY] attribute
 * @receiver Channel
 * @return Session
 */
fun Channel.getSession(): Session {
    return attr(Session.SESSION_KEY).get()
}

/**
 * Sets the [Session.SESSION_KEY] attribute
 * @receiver Channel
 * @param session Session
 */
fun Channel.setSession(session: Session) {
    attr(Session.SESSION_KEY).set(session)
}
