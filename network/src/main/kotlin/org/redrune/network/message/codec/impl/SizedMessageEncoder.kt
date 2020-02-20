package org.redrune.network.message.codec.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.Codec
import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageEncoder
import org.redrune.network.packet.access.PacketBuilder

/**
 * Messages that are encoded with the size must use this codec
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 20, 2020
 */
@ChannelHandler.Sharable
class SizedMessageEncoder(private val codec: Codec) : MessageToByteEncoder<Message>() {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        val encoder = codec.encoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.warn { "Unable to find encoder! [msg=$msg, codec=${codec.javaClass.simpleName}]" }
            return
        }
        val builder = PacketBuilder(buffer = out)
        encoder.encode(builder, msg)
        builder.writeSize()
        logger.info { "Encoding successful [encoder=${encoder.javaClass.simpleName}, msg=$msg, codec=${codec.javaClass.simpleName}" }
    }

}