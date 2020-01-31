package org.redrune.network.model.packet.game

import io.netty.buffer.ByteBuf
import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.PacketType

/**
 * A game packet is a packet that is received while a player is in the game
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class GamePacket(val opcode: Int = -1, val type: PacketType, payload: ByteBuf) : Packet(payload) {


}