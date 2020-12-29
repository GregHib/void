package rs.dusk.core.network.codec.packet.access

import rs.dusk.buffer.read.BufferReader
import java.nio.ByteBuffer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since February 18, 2020
 */
class PacketReader(val opcode: Int, buffer: ByteBuffer) : BufferReader(buffer) {

    constructor(opcode: Int, byteArray: ByteArray) : this(opcode, ByteBuffer.wrap(byteArray))

    override fun toString(): String {
        return "PacketReader[opcode=$opcode, buffer=$buffer]"
    }

}