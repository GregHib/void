package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REFLECTION_RESPONSE
import rs.dusk.network.rs.codec.game.decode.message.ReflectionResponseMessage

@PacketMetaData(opcodes = [REFLECTION_RESPONSE], length = VARIABLE_LENGTH_BYTE)
class ReflectionResponseMessageDecoder : GameMessageDecoder<ReflectionResponseMessage>() {

    override fun decode(packet: PacketReader): ReflectionResponseMessage {
        packet.readByte()//0
        return ReflectionResponseMessage()
    }
}