package org.redrune.network.packet.type

import io.netty.buffer.ByteBuf
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class GamePacket(val opcode: Int, val type: PacketType, override val buffer: ByteBuf) : Packet(buffer)