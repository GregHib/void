package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.UnknownMessage

class UnknownMessageDecoder : MessageDecoder<UnknownMessage>(2) {

    override fun decode(packet: PacketReader): UnknownMessage {
        return UnknownMessage(packet.readShort())
    }

}