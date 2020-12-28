package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class ChatTypeMessageDecoder : MessageDecoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.changeChatType(
            context,
            packet.readUnsignedByte()
        )
    }

}