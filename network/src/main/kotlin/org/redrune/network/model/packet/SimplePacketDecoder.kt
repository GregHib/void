package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf
import org.redrune.network.codec.Codec

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class SimplePacketDecoder(codec: Codec) : PacketDecoder(codec) {

    override fun readOpcode(buf: ByteBuf): Int {
        return buf.readUnsignedByte().toInt()
    }

    override fun expectedLength(opcode: Int, buf: ByteBuf): Int {
        val expected = codec.getLength(opcode)
        return if (expected < 0) {
            when (expected) {
                -1 -> buf.readUnsignedByte().toInt()
                -2 -> buf.readUnsignedShort()
                -3 -> buf.readUnsignedInt().toInt()
                else -> throw IllegalStateException("Expected packet length is between [-1 - -3], we received $expected")
            }
        } else {
            expected
        }
    }
}