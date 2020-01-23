package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.network.packet.struct.PacketHeader

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
     * The [PacketHeader] of the packet, default to [PacketHeader.STANDARD]
     */
    private val header: PacketHeader = PacketHeader.STANDARD,

    /**
     * The buffer of the packet
     */
    private val buffer: ByteBuf = Unpooled.buffer()
) {

    /**
     * Writing a byte to the [buffer]
     */
    fun writeByte(value: Int): PacketBuilder {
        buffer.writeByte(value)
        return this
    }

    /**
     * Writing an integer to the [buffer]
     */
    fun writeInt(value: Int): PacketBuilder {
        buffer.writeInt(value)
        return this
    }

    /**
     * Writing a long to the [buffer]
     */
    fun writeLong(value: Long): PacketBuilder {
        buffer.writeLong(value)
        return this
    }

    /**
     * Write the bytes of another buffer to the [buffer]
     */
    fun writeBytes(other: ByteBuf): PacketBuilder {
        buffer.writeBytes(other)
        return this
    }

    /**
     * Gets the writer index of the [buffer]
     */
    fun position(): Int {
        return buffer.writerIndex()
    }

    /**
     * Writes a short to the [buffer]
     */
    fun writeShort(value: Int): PacketBuilder {
        buffer.writeShort(value)
        return this
    }

    /**
     * Writing a string to the [buffer]
     */
    fun writeString(value: String): PacketBuilder {
        buffer.writeBytes(value.toByteArray())
        buffer.writeByte(0)
        return this
    }

    /**
     * Writing a GJ string to the [buffer]
     */
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