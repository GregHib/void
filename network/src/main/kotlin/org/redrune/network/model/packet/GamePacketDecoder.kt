package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf
import org.redrune.network.codec.Codec
import org.redrune.tools.crypto.IsaacRandom

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
class GamePacketDecoder(codec: Codec, private val cipher: IsaacRandom) : PacketDecoder(codec) {

    override fun readOpcode(buf: ByteBuf): Int {
        return (buf.readUnsignedByte().toInt() - cipher.nextInt()) and 0xff
    }

}