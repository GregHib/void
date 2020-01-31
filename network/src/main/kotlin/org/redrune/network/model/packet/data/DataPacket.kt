package org.redrune.network.model.packet.data

import io.netty.buffer.ByteBuf
import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.game.GamePacket

/**
 * A data packet is a plain buffer that has no additional contextual information (in comparison to a [GamePacket])
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class DataPacket(payload: ByteBuf) : Packet(payload)