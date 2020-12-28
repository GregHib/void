package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadingMessage

class RegionLoadingMessageDecoder : MessageDecoder<RegionLoadingMessage>(4) {

    override fun decode(packet: PacketReader): RegionLoadingMessage {
        //1057001181
        return RegionLoadingMessage
    }

}