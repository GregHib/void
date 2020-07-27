package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.HYPERLINK_TEXT
import rs.dusk.network.rs.codec.game.decode.message.HyperlinkMessage

@PacketMetaData(opcodes = [HYPERLINK_TEXT], length = PacketType.VARIABLE_LENGTH_BYTE)
class HyperlinkMessageDecoder : GameMessageDecoder<HyperlinkMessage>() {

    override fun decode(packet: PacketReader) = HyperlinkMessage(packet.readString(), packet.readString(), packet.readByte())

}