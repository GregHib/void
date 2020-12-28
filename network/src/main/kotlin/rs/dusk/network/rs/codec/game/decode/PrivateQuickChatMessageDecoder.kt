package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.PrivateQuickChatMessage

class PrivateQuickChatMessageDecoder : GameMessageDecoder<PrivateQuickChatMessage>(VARIABLE_LENGTH_BYTE) {

    override fun decode(packet: PacketReader) : PrivateQuickChatMessage {
        val username = packet.readString()
        val file = packet.readUnsignedShort()
        val data = if(packet.readableBytes() > 0) ByteArray(packet.readableBytes()) else null
        if(data != null) {
            packet.readBytes(data)
        }
        return PrivateQuickChatMessage(username, file, data)
    }

}