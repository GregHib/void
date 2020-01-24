package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class PacketBuilder(

    /**
     * The opcode of the packet, default as -1
     */
    private val opcode: Int = -1,

    /**
     * The [PacketHeader] of the packet, default to [PacketHeader.FIXED]
     */
    private val header: PacketHeader = PacketHeader.FIXED,

    /**
     * The buffer of the packet
     */
    private val buffer: ByteBuf = Unpooled.buffer()
) {
    fun writeByte(value: Int): PacketBuilder {
        buffer.writeByte(value)
        return this
    }

    fun writeInt(value: Int) {
        buffer.writeInt(value)
    }

    fun writeLong(value: Long): PacketBuilder {
        buffer.writeLong(value)
        return this
    }

    fun writeBytes(other: ByteBuf): PacketBuilder {
        buffer.writeBytes(other)
        return this
    }

    fun position(): Int {
        return buffer.writerIndex()
    }

    fun writeShort(value: Int): PacketBuilder {
        buffer.writeShort(value)
        return this
    }

    fun writeString(value: String): PacketBuilder {
        buffer.writeBytes(value.toByteArray())
        buffer.writeByte(0)
        return this
    }

    fun writeGJString(value: String): PacketBuilder {
        buffer.writeByte(0)
        buffer.writeBytes(value.toByteArray())
        buffer.writeByte(0)
        return this
    }

    /**
     * Constructs a [Packet] from the [buffer]
     * @return Packet
     */
    fun build(): Packet = Packet(opcode = opcode, header = header, buffer = buffer)
}