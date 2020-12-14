package rs.dusk.core.network.codec.packet.access

import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.network.model.packet.PacketType
import java.nio.ByteBuffer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class PacketReader(val opcode: Int = -1, val type: PacketType = PacketType.RAW, buffer: ByteBuffer) : BufferReader(buffer) {

    constructor(opcode: Int = -1, type: PacketType = PacketType.RAW, byteArray: ByteArray) : this(opcode, type, ByteBuffer.wrap(byteArray))

    override fun toString(): String {
        return "PacketReader[opcode=$opcode, buffer=$buffer]"
    }

}