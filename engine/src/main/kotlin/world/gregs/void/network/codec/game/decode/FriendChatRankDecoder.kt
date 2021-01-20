package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder
import world.gregs.void.network.packet.PacketSize.BYTE

class FriendChatRankDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.rankFriendsChat(
            context,
            packet.readString(),
            packet.readByte(Modifier.INVERSE)
        )
    }

}