package rs.dusk.core.network.codec.packet

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import rs.dusk.core.crypto.IsaacCipher
import rs.dusk.core.network.codec.getCipherIn
import rs.dusk.core.network.codec.packet.access.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class PacketDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    private enum class State {
        Opcode,
        Length,
        Payload
    }

    /**
     * The current state of the decoder
     */
    private var state: State = State.Opcode

    /**
     * The read opcode of the packet
     */
    private var opcode = -1

    /**
     * The expected length of the packet
     */
    protected var length = -1

    /**
     * The type of packet
     */
    private var type = -3

    /**
     * Handles reading the opcode from the buffer
     */
    abstract fun readOpcode(buf: ByteBuf, cipher: IsaacCipher?): Int

    /**
     * Getting the expected length of a buffer by the opcode identified [opcode]. If th
     */
    abstract fun getExpectedLength(ctx : ChannelHandlerContext, opcode : Int): Int?

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (state == State.Opcode) {
            if (!buf.isReadable) {
                logger.error { "Unable to decode opcode from buffer - buffer is not readable." }
                return
            }
            opcode = readOpcode(buf, ctx.channel().getCipherIn())
            length = getExpectedLength(ctx, opcode) ?: return
            type = length
            state = if (length < 0) State.Length else State.Payload
            logger.debug { "Identified opcode! [opcode=$opcode, expectedLength=$length, readable=${buf.readableBytes()}, nextState=$state]" }
        }
        if (state == State.Length) {
            if (buf.readableBytes() < if (length == -1) 1 else 2) {
                logger.error { "Unable to decode length from buffer [opcode=$opcode] - buffer is not readable [readable=${buf.readableBytes()}]." }
                return
            }
            // when the packet is of a variable length, the expected length is overwritten by the length encoded next
            length = when (type) {
                -1 -> buf.readUnsignedByte().toInt()
                -2 -> buf.readUnsignedShort()
                else -> throw IllegalStateException("Decoding length from packet #$opcode with type $type!")
            }
            logger.debug { "Identified length! [opcode=$opcode, length=$length, type=$type]" }
            state = State.Payload
        }
        if (state == State.Payload) {
            if (buf.readableBytes() < length) {
                logger.error { "Unable to decode payload from buffer - length=$length, readable=${buf.readableBytes()}." }
                return
            }

            //Copy from unsafe buffer
            val payload = ByteArray(length)
            buf.getBytes(buf.readerIndex(), payload, 0, length)
            buf.readerIndex(buf.readerIndex() + length)

            //Handle data
            out.add(PacketReader(opcode, payload))

            //Reset state
            state = State.Opcode

            logger.debug { "Finished and pushed. remaining readable = ${buf.readableBytes()} [opcode=$opcode] " }
        }
    }

}