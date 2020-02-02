package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.redrune.network.codec.CodecRepository

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
abstract class NetworkHandler<I>(val codecRepository: CodecRepository) : SimpleChannelInboundHandler<I>() {

    private val logger = InlineLogger()

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.info { "Channel registered: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        logger.info { "Channel unregistered: " + ctx.channel().remoteAddress() + "." }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    fun send(ctx: ChannelHandlerContext, msg: Any): ChannelFuture? {
        return ctx.pipeline().writeAndFlush(msg)
    }
}