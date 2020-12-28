package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.LatencyMessage

class LatencyMessageDecoder : GameMessageDecoder<LatencyMessage>(2) {

    override fun decode(packet: PacketReader) = LatencyMessage(packet.readShort())

}