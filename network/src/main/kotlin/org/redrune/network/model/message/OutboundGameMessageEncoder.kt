package org.redrune.network.model.message

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.Codec
import org.redrune.tools.crypto.IsaacRandom

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */

class OutboundGameMessageEncoder(
    val codec: Codec,
    val outCipher: IsaacRandom
) : MessageToByteEncoder<Message>() {

    private val logger = InlineLogger()

    @Suppress("UNCHECKED_CAST")
    override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {

    }
}