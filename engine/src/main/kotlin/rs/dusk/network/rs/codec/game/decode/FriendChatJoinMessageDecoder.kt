package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.BYTE

class FriendChatJoinMessageDecoder : MessageDecoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.joinFriendsChat(
            context,
            packet.readString()
        )
    }

}