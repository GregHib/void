package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SCRIPT_4701
import rs.dusk.network.rs.codec.game.decode.message.UnknownScriptMessage

@PacketMetaData(opcodes = [SCRIPT_4701], length = VARIABLE_LENGTH_BYTE)
class UnknownScriptMessageDecoder : GameMessageDecoder<UnknownScriptMessage>() {

    override fun decode(packet: PacketReader) = UnknownScriptMessage(packet.readString())

}