package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.TOOLKIT_PREFERENCES
import rs.dusk.network.rs.codec.game.decode.message.ToolkitPreferencesMessage

@PacketMetaData(opcodes = [TOOLKIT_PREFERENCES], length = VARIABLE_LENGTH_BYTE)
class ToolkitPreferencesMessageDecoder : GameMessageDecoder<ToolkitPreferencesMessage>() {

    override fun decode(packet: PacketReader): ToolkitPreferencesMessage {
        packet.readByte()//0
        return ToolkitPreferencesMessage()
    }

}