package org.redrune.network.packet.codec.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import org.redrune.network.codec.Codec
import org.redrune.network.packet.codec.PacketDecoder
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * This packet decoder decodes runescape packets which are built in this manner [opcode, length, buffer], with the opcode decryption requiring an [IsaacCipher]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class RSPacketDecoder(private val codec: Codec, private val cipher: IsaacCipher) : PacketDecoder() {

    private val logger = InlineLogger()

    override fun readOpcode(buf: ByteBuf): Int {
        return (buf.readUnsignedByte().toInt() - cipher.nextInt()) and 0xff
    }

    override fun getExpectedLength(buf: ByteBuf, opcode: Int): Int? {
        val decoder = codec.decoder(opcode)
        if (decoder == null) {
            logger.warn {  "Unable to identify length of packet [opcode=$opcode, codec=${codec.javaClass.simpleName}]" }
            return null
        }
        return decoder.length
    }
}