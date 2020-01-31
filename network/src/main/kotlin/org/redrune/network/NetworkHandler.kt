package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = InlineLogger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }
}