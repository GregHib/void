package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.decode.message.PublicQuickChatMessage

class PublicQuickChatMessageDecoder : MessageDecoder<PublicQuickChatMessage>(VARIABLE_LENGTH_BYTE) {

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