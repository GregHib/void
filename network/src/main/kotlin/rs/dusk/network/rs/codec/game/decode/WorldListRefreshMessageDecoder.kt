package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.WorldListRefreshMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 19, 2020
 */
class WorldListRefreshMessageDecoder : GameMessageDecoder<WorldListRefreshMessage>(4) {

    override fun decode(packet: PacketReader) = WorldListRefreshMessage(packet.readInt())

}