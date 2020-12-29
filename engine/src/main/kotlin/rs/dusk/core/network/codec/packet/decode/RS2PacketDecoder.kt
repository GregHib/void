package rs.dusk.core.network.codec.packet.decode

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.crypto.IsaacCipher
import rs.dusk.core.network.codec.getCodec
import rs.dusk.core.network.codec.packet.PacketDecoder

/**
 * This packet decoder decodes runescape packets which are built in this manner [opcode, length, buffer], with the opcode decryption requiring an [IsaacCipher]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class RS2PacketDecoder : PacketDecoder() {

    private val logger = InlineLogger()

    override fun readOpcode(buf : ByteBuf, cipher: IsaacCipher?) : Int {
        return (buf.readUnsignedByte().toInt() - cipher!!.nextInt()) and 0xff
    }

    override fun getExpectedLength(ctx : ChannelHandlerContext, opcode : Int) : Int? {
        val codec = ctx.channel().getCodec()
            ?: throw IllegalStateException("Unable to extract codec from channel - undefined!")

        val decoder = codec.getDecoder(opcode)
        if (decoder == null) {
            logger.error { "Unable to identify length of packet [opcode=$opcode, codec=${codec.javaClass.simpleName}]" }
            return null
        }
        return decoder.length
    }

}