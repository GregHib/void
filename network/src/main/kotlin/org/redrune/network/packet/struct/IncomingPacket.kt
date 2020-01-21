package org.redrune.network.packet.struct

import io.netty.buffer.ByteBuf
import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class IncomingPacket(opcode: Int, header: PacketHeader, buffer: ByteBuf) : Packet(opcode, header, buffer) {
    /**
     * Reads an integer.
     *
     * @return An integer.
     */
    fun readInt(): Int {
        return buffer.readInt()
    }

    /**
     * Reads a long.
     *
     * @return A long.
     */
    fun readLong(): Long {
        return buffer.readLong()
    }

}