package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage

class RegionLoadedMessageDecoder : MessageDecoder<RegionLoadedMessage>(0) {

    override fun decode(packet: PacketReader) = RegionLoadedMessage

}