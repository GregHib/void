package org.redrune.network.message.codec

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.redrune.network.getPipelineContents
import org.redrune.network.codec.Codec
import org.redrune.network.message.Message
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
class ChannelMessageHandler(private val codec: Codec) : SimpleChannelInboundHandler<Message>() {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        try {
            val handler: MessageHandler<Message>? = codec.handler(msg::class) as? MessageHandler<Message>

            if (handler == null) {
                logger.warn { "Unable to find message handler - [msg=$msg], codec=${codec.javaClass.simpleName}" }
                return
            }

            handler.handle(ctx, msg)

            logger.info { "Handled msg $msg with handler ${handler.javaClass.simpleName}, using pipeline${ctx.pipeline().getPipelineContents()}" }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

}