package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf

/**
 * This class represents a
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
data class Packet(
    val opcode: Int,
    val payload: ByteBuf
) {

    /**
     * The initial length of the packet
     */
    val length = payload.readableBytes()

    /**
     *
     * The [PacketType] type of packet
     */
    val type = PacketType.byLength(length)
}