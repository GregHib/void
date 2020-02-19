package org.redrune.network.packet.codec

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.packet.PacketType
import org.redrune.network.packet.PacketType.BYTE
import org.redrune.network.packet.PacketType.SHORT
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
        DecoderState.OPCODE

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
    protected var type = PacketType.FIXED

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (state == DecoderState.OPCODE) {
            if (!buf.isReadable) {
                logger.warn { "Unable to decode opcode from buffer - buffer is not readable." }
                return
            }
            opcode = readOpcode(buf)
            length = getExpectedLength(buf, opcode) ?: return
            logger.info { "Identified opcode! [opcode=$opcode, expectedLength=$length]" }
            state = DecoderState.LENGTH
        }
        if (state == DecoderState.LENGTH) {
            type = PacketType.byLength(length)
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
            state = DecoderState.BUFFER
        }
        if (state == DecoderState.BUFFER) {
            if (buf.readableBytes() < length) {
                return
            }
            if (length > buf.readableBytes()) {
                logger.warn { "Unable to decode payload from buffer - length=$length, readable=${buf.readableBytes()}." }
                return
            }

            //Copy from unsafe buffer
            val buffer = buf.readBytes(length)

            //Handle data
            out.add(PacketReader(opcode, type, buffer))

            //Reset state
            state = DecoderState.OPCODE
        }
    }

    abstract fun readOpcode(buf: ByteBuf): Int

    abstract fun getExpectedLength(buf: ByteBuf, opcode: Int): Int?
}