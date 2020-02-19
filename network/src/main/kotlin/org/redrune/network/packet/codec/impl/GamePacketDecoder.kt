package org.redrune.network.packet.codec.impl

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import org.redrune.network.codec.Codec
import org.redrune.network.packet.codec.PacketDecoder
import org.redrune.tools.crypto.cipher.IsaacCipher

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GamePacketDecoder(private val codec: Codec, private val cipher: IsaacCipher) : PacketDecoder() {

    private val logger = InlineLogger()

    override fun readOpcode(buf: ByteBuf): Int {
        return (buf.readUnsignedByte().toInt() - cipher.nextInt()) and 0xff
    }

    override fun getExpectedLength(buf: ByteBuf, opcode: Int): Int? {
        val decoder = codec.decoder(opcode)
        if (decoder == null) {
            logger.warn {  "Unable to identify length of packet [opcode=$opcode]" }
            return null
        }
        return decoder.length
    }
}