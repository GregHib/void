package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.WORLD_MAP_CLICK
import rs.dusk.network.rs.codec.game.decode.message.WorldMapCloseMessage

@PacketMetaData(opcodes = [WORLD_MAP_CLICK], length = 4)
class WorldMapOpenMessageDecoder : GameMessageDecoder<WorldMapCloseMessage>() {

    override fun decode(packet: PacketReader) = WorldMapCloseMessage()

}