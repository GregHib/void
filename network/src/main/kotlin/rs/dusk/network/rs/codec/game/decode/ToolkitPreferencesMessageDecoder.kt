package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.decode.message.ToolkitPreferencesMessage

class ToolkitPreferencesMessageDecoder : MessageDecoder<ToolkitPreferencesMessage>(VARIABLE_LENGTH_BYTE) {

    override fun decode(packet: PacketReader): ToolkitPreferencesMessage {
        packet.readByte()//0
        return ToolkitPreferencesMessage()
    }

}