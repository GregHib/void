package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ADD_IGNORE
import rs.dusk.network.rs.codec.game.decode.message.IgnoreListAddMessage

@PacketMetaData(opcodes = [ADD_IGNORE], length = PacketType.VARIABLE_LENGTH_BYTE)
class IgnoreListAddMessageDecoder : GameMessageDecoder<IgnoreListAddMessage>() {

    override fun decode(packet: PacketReader) = IgnoreListAddMessage(packet.readString(), packet.readBoolean())

}