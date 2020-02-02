package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
abstract class NetworkHandler<I> : SimpleChannelInboundHandler<I>() {

    private val logger = InlineLogger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }
}