package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader
import rs.dusk.network.packet.PacketSize.BYTE

class FriendListAddDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.addFriend(
            context,
            packet.readString()
        )
    }

}