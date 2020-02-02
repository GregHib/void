package org.redrune.network.codec.handshake

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class HandshakeDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        ctx.pipeline().remove(this)

        val opcode = msg.readUnsignedByte().toInt()
        val requestType = HandshakeRequestType.valueOf(opcode)

        var additionalBuf: ByteBuf? = null
        if (msg.isReadable) {
            additionalBuf = msg.readBytes(msg.readableBytes())
            logger.info { "Additional buf found $additionalBuf" }
        }
        if (requestType == null) {
            logger.warn { "Unable to identify request type #$opcode" }
        } else {
            logger.info { "Request $requestType identified!" }
            out.add(HandshakeRequestMessage(requestType, additionalBuf ?: Unpooled.EMPTY_BUFFER))
        }

    }
}