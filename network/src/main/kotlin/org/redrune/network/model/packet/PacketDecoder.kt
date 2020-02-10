package org.redrune.network.model.packet

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.Codec
import org.redrune.network.getHexContents
import org.redrune.network.model.packet.PacketDecoder.DecodeState.*
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
abstract class PacketDecoder(val codec: Codec) : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    /**
     * The state the decoder is in
     */
    private var state = DECODE_OPCODE

    /**
     * The opcode of the packet we're currently decoding
     */
    private var opcode = -1

    /**
     * The length of the packet we're currently decoding
     */
    private var length = 0

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        try {
            logger.debug { "Packet(opcode=$opcode, length=$length), state=$state\n${buf.getHexContents()}" }
            if (state == DECODE_OPCODE) {
                if (!buf.isReadable) {
                    logger.warn { "Unable to decode opcode from buffer - buffer is not readable." }
                    return
                }
                opcode = buf.readUnsignedByte().toInt()
                state = DECODE_LENGTH
            }
            if (state == DECODE_LENGTH) {
                if (!buf.isReadable) {
                    logger.warn { "Unable to decode length from buffer - buffer is not readable (opcode=$opcode)." }
                    out.add(PacketReader(Packet(opcode, Unpooled.EMPTY_BUFFER)))
                    return
                }
                length = expectedLength(opcode, buf)
                logger.info { "found length $length" }
                state = DECODE_PAYLOAD
            }
            if (state == DECODE_PAYLOAD) {
                if (length > buf.readableBytes()) {
                    logger.warn { "Unable to decode payload from buffer - length=$length, readable=${buf.readableBytes()}." }
                    return
                }
                val payload = buf.readBytes(length)
                state = DECODE_OPCODE

                val packet = Packet(opcode, payload)
                logger.debug { "Constructed packet $packet" }
                out.add(PacketReader(packet))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Reads the opcode of a buffer
     * @return Int The opcode
     */
    open fun readOpcode(buf: ByteBuf): Int {
        return buf.readUnsignedByte().toInt()
    }

    /**
     * Finds the expected length of a packet by the opcode and returns it
     */
    open fun expectedLength(opcode: Int, buf: ByteBuf): Int {
        val expected = codec.getLength(opcode)
        return if (expected < 0) {
            when (expected) {
                -1 -> buf.readUnsignedByte().toInt()
                -2 -> buf.readUnsignedShort()
                -3 -> buf.readUnsignedInt().toInt()
                else -> throw IllegalStateException("Expected packet length is between [-1 - -3], we received $expected")
            }
        } else {
            expected
        }
    }


    /**
     * The decoder can be in any of these states at any time
     */
    private enum class DecodeState {
        DECODE_OPCODE, DECODE_LENGTH, DECODE_PAYLOAD
    }
}