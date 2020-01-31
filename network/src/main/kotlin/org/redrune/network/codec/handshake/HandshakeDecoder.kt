package org.redrune.network.codec.handshake

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class HandshakeDecoder : MessageToMessageDecoder<PacketReader>() {

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, packet: PacketReader, out: MutableList<Any>) {
        ctx.pipeline().remove(this)

        val opcode = packet.readUnsignedByte()
        val requestType = HandshakeRequestType.valueOf(opcode)

        var additionalBuf: ByteBuf? = null
        if (packet.isReadable()) {
            additionalBuf = packet.readBytes(packet.readableBytes())
            logger.info { "Additional buf found $additionalBuf" }
        }
        if (requestType == null) {
            logger.warn { "Unable to identify request type #$opcode" }
        } else {
            logger.info { "Request $requestType identified!" }
            out.add(HandshakeRequestMessage(requestType, DefaultByteBufHolder(additionalBuf ?: Unpooled.EMPTY_BUFFER)))
        }

    }
}