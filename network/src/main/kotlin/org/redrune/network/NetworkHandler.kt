package org.redrune.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import mu.KotlinLogging
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkHandler : SimpleChannelInboundHandler<Message>() {

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


    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        ctx.channel().attr(Session.SESSION_KEY).get().messageReceived(msg)
    }

}