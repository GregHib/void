package org.redrune.network.packet.codec.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import org.redrune.network.codec.Codec
import org.redrune.network.packet.codec.PacketDecoder

/**
 * Handling the decoding of a simple packet, which is a packet whose contents are [opcode, buffer]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class SimplePacketDecoder(private val codec: Codec) : PacketDecoder() {

    private val logger = InlineLogger()

    override fun readOpcode(buf: ByteBuf): Int {
        return buf.readUnsignedByte().toInt()
    }

    override fun getExpectedLength(buf: ByteBuf, opcode: Int): Int? {
        val decoder = codec.decoder(opcode)
        if (decoder == null) {
            logger.warn { "Unable to identify length of packet [opcode=$opcode]" }
            return null
        }
        return decoder.length
    }
}