package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.DONE_LOADING_REGION
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage

@PacketMetaData(opcodes = [DONE_LOADING_REGION], length = 0)
class RegionLoadedMessageDecoder : GameMessageDecoder<RegionLoadedMessage>() {

    override fun decode(packet: PacketReader) = RegionLoadedMessage

}