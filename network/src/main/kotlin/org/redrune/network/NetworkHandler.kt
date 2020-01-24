package org.redrune.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import mu.KotlinLogging

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = KotlinLogging.logger {}

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.channel().close()
        logger.info("channel closed")
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        ctx.channel().attr(Session.SESSION_KEY).get().messageReceived(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable) {
        logger.error("Exception occurred for channel: " + ctx.channel() + ", closing...", e.printStackTrace())
        ctx.channel().close()
    }

}