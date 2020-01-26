package org.redrune.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import mu.KotlinLogging

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = KotlinLogging.logger {}

    override fun channelActive(ctx: ChannelHandlerContext) {
//        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.channel().close()
//        logger.info("channel closed")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable) {
        e.printStackTrace()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        try {
            ctx.channel().attr(Session.SESSION_KEY).get().messageReceived(msg)
            ctx.channel().attr(Session.SESSION_KEY).get().printPipeline()
            println("read $msg")
        } finally {
            ReferenceCountUtil.retain(msg)
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

}