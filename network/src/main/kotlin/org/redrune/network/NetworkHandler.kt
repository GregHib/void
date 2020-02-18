package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.redrune.network.codec.Codec
import org.redrune.network.model.message.Message
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
class NetworkHandler(private val codec: Codec) : SimpleChannelInboundHandler<Message>() {

    private val logger = InlineLogger()

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.debug { "Channel registered: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.debug { "Channel active: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        logger.debug { "Channel unregistered: " + ctx.channel().remoteAddress() + "." }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.debug { "Channel inactive: " + ctx.channel().remoteAddress() + "." }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
//        ctx.close()
    }


    @Suppress("UNCHECKED_CAST")
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        logger.info { "Attempting to read message $msg" }
        try {
            val handler = codec.handle(msg::class, ctx, msg)

            logger.info { "Handled msg $msg with handler $handler" }
            logger.info { "Pipeline = ${ctx.pipeline().getPipelineContents()}" }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}