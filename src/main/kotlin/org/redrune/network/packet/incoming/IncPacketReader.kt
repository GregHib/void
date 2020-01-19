package org.redrune.network.packet.incoming

import org.redrune.game.architecture.Player
import org.redrune.network.packet.PacketContext
import org.redrune.network.packet.struct.IncomingPacket

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-18
 */
abstract class IncPacketReader<P: PacketContext> {

    /**
     * Handling the reading of an [IncomingPacket]
     * @param player Player The player who read the packet
     * @param packet IncomingPacket The incoming packet
     */
    abstract fun read(player: Player, packet: IncomingPacket): P

}