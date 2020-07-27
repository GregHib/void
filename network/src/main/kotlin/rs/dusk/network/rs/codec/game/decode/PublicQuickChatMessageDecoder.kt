package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.QUICK_PUBLIC_MESSAGE
import rs.dusk.network.rs.codec.game.decode.message.PublicQuickChatMessage

@PacketMetaData(opcodes = [QUICK_PUBLIC_MESSAGE], length = PacketType.VARIABLE_LENGTH_BYTE)
class PublicQuickChatMessageDecoder : GameMessageDecoder<PublicQuickChatMessage>() {

    override fun decode(packet: PacketReader): PublicQuickChatMessage {
        val script = packet.readByte()
        val file = packet.readUnsignedShort()
        val data: ByteArray? = if(packet.readableBytes() > 0) ByteArray(packet.readableBytes()) else null
        if(data != null) {
            packet.readBytes(data)
        }
        return PublicQuickChatMessage(script, file, data)
    }

}