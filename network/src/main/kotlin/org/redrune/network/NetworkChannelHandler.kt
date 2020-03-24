package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.redrune.core.network.codec.Codec
import org.redrune.core.network.message.Message
import org.redrune.core.network.message.codec.MessageHandler
import org.redrune.core.tools.utility.getPipelineContents
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
class NetworkChannelHandler(private val codec: Codec) : SimpleChannelInboundHandler<Message>() {

    private val logger = InlineLogger()

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Channel inactive: " + ctx.channel().remoteAddress() + "." }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

    @Suppress("UNCHECKED_CAST")
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        try {
            val handler: MessageHandler<Message>? = codec.handler(msg::class) as? MessageHandler<Message>

            if (handler == null) {
                logger.warn { "Unable to find message handler - [msg=$msg], codec=${codec.javaClass.simpleName}" }
                return
            }

            handler.handle(ctx, msg)

            logger.info { "Handled successfully[msg=$msg, codec=${codec.javaClass.simpleName}, handler=${handler.javaClass.simpleName}, pipeline=${ctx.pipeline().getPipelineContents()}]" }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}

