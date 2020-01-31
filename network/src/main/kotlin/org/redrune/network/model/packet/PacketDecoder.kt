package org.redrune.network.model.packet

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.model.packet.PacketDecoder.DecodeState.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
abstract class PacketDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    /**
     * The state the decoder is in
     */
    private var state = DECODE_OPCODE

    protected var opcode = -1

    protected var length = 0

    protected var type: PacketType = PacketType.FIXED

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        when (state) {
            DECODE_OPCODE -> {
                if (!buf.isReadable) {
                    logger.warn { "Unable to decode opcode from buffer - buffer is not readable." }
                    return
                }
                opcode = readOpcode(buf)
                state = DECODE_LENGTH
            }
            DECODE_LENGTH -> {
                if (!buf.isReadable) {
                    logger.warn { "Unable to decode length from buffer - buffer is not readable." }
                    return
                }
                val pair = readLength(buf)
                length = pair.first
                type = pair.second
                state = DECODE_PAYLOAD
            }
            DECODE_PAYLOAD -> {
                if (length > buf.readableBytes()) {
                    logger.warn { "Unable to decode payload from buffer - length=$length, readable=${buf.readableBytes()}." }
                    return
                }
                val payload = readPayload(length, buf)
                out.add(constructPacket(opcode, length, type, payload))
                state = DECODE_OPCODE
            }
        }
    }

    abstract fun constructPacket(
        opcode: Int,
        length: Int,
        type: PacketType,
        payload: ByteBuf
    ): PacketReader

    /**
     * When an incoming buffer is received, the opcode is read from this block
     */
    abstract fun readOpcode(buf: ByteBuf): Int

    /**
     * The length of the packet is read from this block
     */
    abstract fun readLength(buf: ByteBuf): Pair<Int, PacketType>

    /**
     * The payload of the packet is read from this block
     */
    open fun readPayload(length: Int, buf: ByteBuf): ByteBuf {
        return buf.readBytes(length)
    }

    /**
     * The decoder can be in any of these states at any time
     */
    private enum class DecodeState {
        DECODE_OPCODE, DECODE_LENGTH, DECODE_PAYLOAD
    }
}