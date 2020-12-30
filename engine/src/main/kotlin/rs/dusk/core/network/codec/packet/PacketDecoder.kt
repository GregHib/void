package rs.dusk.core.network.codec.packet

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import rs.dusk.core.network.codec.getCipherIn
import rs.dusk.core.network.codec.getCodec

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class PacketDecoder : ByteToMessageDecoder() {

    private val logger = InlineLogger()

    private enum class State {
        Opcode,
        Length,
        Payload
    }

    private var state: State = State.Opcode

    private var opcode = -1

    protected var length = -1
    private var type = -3

    /**
     * Getting the expected length of a buffer by the opcode identified [opcode]. If th
     */
    fun getExpectedLength(ctx: ChannelHandlerContext, opcode: Int): Int? {
        val codec = ctx.channel().getCodec()
            ?: throw IllegalStateException("Unable to extract codec from channel - undefined!")

        val decoder = codec.getDecoder(opcode)
        if (decoder == null) {
            logger.error { "Unable to identify length of packet [opcode=$opcode, codec=${codec.javaClass.simpleName}]" }
            return null
        }
        return decoder.length
    }

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (state == State.Opcode) {
            if (!buf.isReadable) {
                logger.error { "Unable to decode opcode from buffer - buffer is not readable." }
                return
            }
            val cipher = ctx.channel().getCipherIn()?.nextInt() ?: 0
            opcode = (buf.readUnsignedByte().toInt() - cipher) and 0xff
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