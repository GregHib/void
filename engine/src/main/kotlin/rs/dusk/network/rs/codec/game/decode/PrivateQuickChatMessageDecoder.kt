package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.BYTE

class PrivateQuickChatMessageDecoder : MessageDecoder(BYTE) {

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