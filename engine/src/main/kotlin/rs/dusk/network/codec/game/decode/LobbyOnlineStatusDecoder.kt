package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class LobbyOnlineStatusDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.lobbyOnlineStatus(
            context,
            packet.readByte(),
            packet.readByte(),
            packet.readByte()
        )
    }

}