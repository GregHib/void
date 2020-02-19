package org.redrune.network.packet.type

import io.netty.buffer.ByteBuf
import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class SimplePacket(val opcode: Int, override val buffer: ByteBuf) : Packet(buffer)