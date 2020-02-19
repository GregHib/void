package org.redrune.network.message.codec.game

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.Codec
import org.redrune.network.message.Message
import org.redrune.network.message.codec.MessageEncoder
import org.redrune.network.packet.access.PacketBuilder
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
class GameMessageEncoder(private val codec: Codec, private val cipher: IsaacCipher) : MessageToByteEncoder<Message>() {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
        val encoder = codec.encoder(msg::class) as? MessageEncoder<Message>
        if (encoder == null) {
            logger.warn { "Unable to find encoder! [msg=$msg]"}
            return
        }
        val builder = PacketBuilder(buffer = out, cipher = cipher)
        encoder.encode(builder, msg)
        builder.writeSize()
    }

}
