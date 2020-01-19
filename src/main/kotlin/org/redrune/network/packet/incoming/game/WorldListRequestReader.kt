package org.redrune.network.packet.incoming.game

import org.redrune.game.architecture.Player
import org.redrune.network.packet.PacketOpcode
import org.redrune.network.packet.context.WorldListUpdateContext
import org.redrune.network.packet.incoming.IncPacketReader
import org.redrune.network.packet.incoming.PacketDefinition
import org.redrune.network.packet.struct.IncomingPacket

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-18
 */
@PacketDefinition([PacketOpcode.WORLDLIST_REQUEST_PACKET])
class WorldListRequestReader : IncPacketReader<WorldListUpdateContext>() {
    override fun read(player: Player, packet: IncomingPacket): WorldListUpdateContext {
        val updateType = packet.readInt()
        return WorldListUpdateContext(updateType)
    }
}