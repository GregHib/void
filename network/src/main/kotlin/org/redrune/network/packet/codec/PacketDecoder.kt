package org.redrune.network.packet.codec

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.packet.PacketType
import org.redrune.network.packet.PacketType.*
import org.redrune.network.packet.access.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class PacketDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    /**
     * The current state of the decoder
     */
    protected var state: DecoderState =
        DecoderState.DECODE_OPCODE

    /**
     * The read opcode of the packet
     */
    protected var opcode = -1

    /**
     * The expected length of the packet
     */
    protected var length = -1

    /**
     * The type of packet
     */
    protected var type = FIXED

    /**
     * Handles reading the opcode from the buffer
     */
    abstract fun readOpcode(buf: ByteBuf): Int

    /**
     * Getting the expected length of a buffer by the opcode identified [opcode]. If th
     */
    abstract fun getExpectedLength(buf: ByteBuf, opcode: Int): Int?

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (state == DecoderState.DECODE_OPCODE) {
            if (!buf.isReadable) {
                logger.warn { "Unable to decode opcode from buffer - buffer is not readable." }
                return
            }
            opcode = readOpcode(buf)
            length = getExpectedLength(buf, opcode) ?: buf.readableBytes()
            type = PacketType.byLength(length)
            state = if (length < 0) DecoderState.DECODE_LENGTH else DecoderState.DECODE_PAYLOAD
            logger.info { "Identified opcode! [opcode=$opcode, expectedLength=$length, readable=${buf.readableBytes()}]" }
        }
        if (state == DecoderState.DECODE_LENGTH) {
            if (buf.readableBytes() < if(length == -1) 1 else 2) {
                logger.warn { "Unable to decode length from buffer [opcode=$opcode] - buffer is not readable [readable=${buf.readableBytes()}]." }
                return
            }
            // when the packet is of a variable length, the expected length is overwritten by the length encoded next
            when(type) {
                BYTE -> {
                    length = buf.readUnsignedByte().toInt()
                }
                SHORT -> {
                    length = buf.readUnsignedShort()
                }
            }
            logger.info { "Identified length! [opcode=$opcode, length=$length, type=$type]" }
            state = DecoderState.DECODE_PAYLOAD
        }
        if (state == DecoderState.DECODE_PAYLOAD) {
            if (buf.readableBytes() < length) {
                logger.warn { "Unable to decode payload from buffer - length=$length, readable=${buf.readableBytes()}." }
                return
            }

            //Copy from unsafe buffer
            val buffer = buf.readBytes(length)

            //Handle data
            out.add(PacketReader(opcode, type, buffer))

            //Reset state
            state = DecoderState.DECODE_OPCODE

            logger.info { "Finished and pushed. remaining readable = ${buf.readableBytes()} [opcode=$opcode] " }
        }
    }

}