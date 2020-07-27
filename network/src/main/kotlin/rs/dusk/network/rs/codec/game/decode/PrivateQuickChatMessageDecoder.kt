package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.QUICK_PRIVATE_MESSAGE
import rs.dusk.network.rs.codec.game.decode.message.PrivateQuickChatMessage

@PacketMetaData(opcodes = [QUICK_PRIVATE_MESSAGE], length = PacketType.VARIABLE_LENGTH_BYTE)
class PrivateQuickChatMessageDecoder : GameMessageDecoder<PrivateQuickChatMessage>() {

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