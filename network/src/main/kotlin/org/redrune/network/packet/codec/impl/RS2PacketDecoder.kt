package org.redrune.network.packet.codec.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import org.redrune.network.codec.Codec
import org.redrune.network.packet.codec.PacketDecoder
import org.redrune.tools.crypto.ISAACCipher
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * This packet decoder decodes runescape packets which are built in this manner [opcode, length, buffer], with the opcode decryption requiring an [IsaacCipher]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class RS2PacketDecoder(private val codec: Codec, private val cipher: ISAACCipher) : PacketDecoder() {

    private val logger = InlineLogger()

    override fun readOpcode(buf: ByteBuf): Int {
        println("attempting to read opcode of a rs2 packet with cipher=${cipher.seed.contentToString()}")
        return (buf.readUnsignedByte().toInt() - cipher.nextInt()) and 0xff
    }

    override fun getExpectedLength(opcode: Int): Int? {
        val decoder = codec.decoder(opcode)
        if (decoder == null) {
            logger.warn { "Unable to identify length of packet [opcode=$opcode, codec=${codec.javaClass.simpleName}]" }
            return null
        }
        return decoder.length
    }
}