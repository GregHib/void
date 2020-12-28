package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.WorldMapCloseMessage

class WorldMapOpenMessageDecoder : MessageDecoder<WorldMapCloseMessage>(4) {

    override fun decode(packet: PacketReader) = WorldMapCloseMessage()

}