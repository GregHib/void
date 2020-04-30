package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ENTER_INTEGER
import rs.dusk.network.rs.codec.game.decode.message.IntegerEntryMessage

@PacketMetaData(opcodes = [ENTER_INTEGER], length = 4)
class IntegerEntryMessageDecoder : GameMessageDecoder<IntegerEntryMessage>() {

    override fun decode(packet: PacketReader) = IntegerEntryMessage(packet.readInt())

}