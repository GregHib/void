package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REGION_LOADING
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadingMessage

@PacketMetaData(opcodes = [REGION_LOADING], length = 4)
class RegionLoadingMessageDecoder : GameMessageDecoder<RegionLoadingMessage>() {

    override fun decode(packet: PacketReader): RegionLoadingMessage {
        //1057001181
        return RegionLoadingMessage
    }

}