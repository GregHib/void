package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.network.packet.struct.PacketHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
class PacketReader(private val opcode: Int = -1, private val header: PacketHeader = PacketHeader.STANDARD, private val buffer: ByteBuf) {

    constructor(packet: Packet) : this(opcode = packet.opcode, header = packet.header, buffer = packet.buffer)
    constructor(array: ByteArray) : this(buffer = Unpooled.wrappedBuffer(array))

    fun readByte(): Byte {
        return buffer.readByte()
    }

}