package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.STRING_ENTRY
import rs.dusk.network.rs.codec.game.decode.message.StringEntryMessage

@PacketMetaData(opcodes = [STRING_ENTRY], length = VARIABLE_LENGTH_BYTE)
class StringEntryMessageDecoder : GameMessageDecoder<StringEntryMessage>() {

    override fun decode(packet: PacketReader) = StringEntryMessage(packet.readString())

}