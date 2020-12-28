package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE

class PrivateQuickChatMessageDecoder : MessageDecoder(VARIABLE_LENGTH_BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.privateQuickChat(
            context = context,
            name = packet.readString(),
            file = packet.readUnsignedShort(),
            data = ByteArray(packet.readableBytes()).apply {
                packet.readBytes(this)
            }
        )
    }

}