package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.MOVE_MOUSE
import rs.dusk.network.rs.codec.game.decode.message.MovedMouseMessage

@PacketMetaData(opcodes = [MOVE_MOUSE], length = VARIABLE_LENGTH_BYTE)
class MovedMouseMessageDecoder : GameMessageDecoder<MovedMouseMessage>() {

    override fun decode(packet: PacketReader) = MovedMouseMessage()

}