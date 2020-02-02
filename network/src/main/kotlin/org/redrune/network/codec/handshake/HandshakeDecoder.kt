package org.redrune.network.codec.handshake

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.handshake.message.HandshakeRequestMessage
import org.redrune.network.codec.handshake.message.HandshakeRequestType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class HandshakeDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        ctx.pipeline().remove(this)

        val opcode = buf.readUnsignedByte().toInt()
        val requestType = HandshakeRequestType.valueOf(opcode)

        var additionalBuf: ByteBuf? = null
        if (buf.isReadable()) {
            additionalBuf = buf.readBytes(buf.readableBytes())
            logger.info { "Additional buf found $additionalBuf" }
        }
        if (requestType == null) {
            logger.warn { "Unable to identify request type #$opcode" }
        } else {
            logger.info { "Request $requestType identified!" }
            out.add(
                HandshakeRequestMessage(
                    requestType,
                    additionalBuf ?: Unpooled.EMPTY_BUFFER
                )
            )
        }

    }
}