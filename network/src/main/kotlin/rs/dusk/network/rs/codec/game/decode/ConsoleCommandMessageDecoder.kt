package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CONSOLE_COMMAND
import rs.dusk.network.rs.codec.game.decode.message.ConsoleCommandMessage

@PacketMetaData(opcodes = [CONSOLE_COMMAND], length = VARIABLE_LENGTH_BYTE)
class ConsoleCommandMessageDecoder : GameMessageDecoder<ConsoleCommandMessage>() {

    override fun decode(packet: PacketReader): ConsoleCommandMessage {
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        return ConsoleCommandMessage(packet.readString())
    }

}