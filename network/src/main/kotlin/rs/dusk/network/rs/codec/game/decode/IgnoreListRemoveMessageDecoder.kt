package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REMOVE_IGNORE
import rs.dusk.network.rs.codec.game.decode.message.IgnoreListRemoveMessage

@PacketMetaData(opcodes = [REMOVE_IGNORE], length = PacketType.VARIABLE_LENGTH_BYTE)
class IgnoreListRemoveMessageDecoder : GameMessageDecoder<IgnoreListRemoveMessage>() {

    override fun decode(packet: PacketReader) = IgnoreListRemoveMessage(packet.readString())

}