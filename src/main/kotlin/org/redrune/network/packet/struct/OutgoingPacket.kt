package org.redrune.network.packet.struct

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
open class OutgoingPacket(opcode: Int, header: PacketHeader) : Packet(opcode, header, Unpooled.buffer()) {

    /**
     * Constructing an outgoing packet with -1 for the opcode and a standard header
     */
    constructor() : this(-1)

    /**
     * Constructing an outgoing packet by only specifying the opcode, the header will default to standard
     */
    constructor(opcode: Int) : this(opcode, PacketHeader.STANDARD)

    /**
     * Writing a byte to the [buffer]
     */
    fun writeByte(value: Int): OutgoingPacket {
        buffer.writeByte(value)
        return this
    }

    /**
     * Writing an integer to the [buffer]
     */
    fun writeInt(value: Int): OutgoingPacket {
        buffer.writeInt(value)
        return this
    }

    /**
     * Writing a long to the [buffer]
     */
    fun writeLong(value: Long): OutgoingPacket {
        buffer.writeLong(value)
        return this
    }

    /**
     * Write the bytes of another buffer to the [buffer]
     */
    fun writeBytes(other: ByteBuf): OutgoingPacket {
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
    fun writeShort(value: Int): OutgoingPacket {
        buffer.writeShort(value)
        return this
    }

    /**
     * Writing a string to the [buffer]
     */
    fun writeString(value: String): OutgoingPacket {
        buffer.writeBytes(value.toByteArray())
        buffer.writeByte(0)
        return this
    }

    /**
     * Writing a GJ string to the [buffer]
     */
    fun writeGJString(value: String) : OutgoingPacket {
        buffer.writeByte(0)
        buffer.writeBytes(value.toByteArray())
        buffer.writeByte(0)
        return this
    }

}