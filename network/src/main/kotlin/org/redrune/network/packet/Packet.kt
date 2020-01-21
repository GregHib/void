package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import org.redrune.network.packet.struct.PacketHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
data class Packet(
        /**
         * The opcode of the packet
         */
        val opcode: Int,
        /**
         * The header of the packet
         */
        val header: PacketHeader,

        /**
         * The buffer of the packet
         */
        val buffer: ByteBuf
) {

    /**
     * The length of the packet
     */
    private val length = buffer.readableBytes()

    /**
     * If the packet is raw, meaning it was built with no opcode
     */
    fun isRaw(): Boolean {
        return opcode == -1
    }

}