package org.redrune.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import mu.KotlinLogging
import org.redrune.network.session.Session
import sun.security.ssl.HandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkHandler : ChannelInboundHandlerAdapter() {

    /**
     * The logger for this class
     */
    private var logger = KotlinLogging.logger {}

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        val session = channel.attr(Session.SESSION_KEY).get()?.onInactive()
        logger.info("Channel disconnected: $channel")
        channel.close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val channel = ctx.channel()
        val attribute = channel.attr(Session.SESSION_KEY)
        val session = attribute.get()
        session?.messageReceived(msg)

        if (msg is HandshakeMessage) {
            println("message recevied was a handshake message!")
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable) {
        logger.warn("Exception caught, closing channel...", e)
        ctx.close()
    }
}