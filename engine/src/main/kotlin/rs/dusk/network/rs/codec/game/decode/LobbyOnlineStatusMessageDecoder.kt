package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class LobbyOnlineStatusMessageDecoder : MessageDecoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.lobbyOnlineStatus(
            context,
            packet.readByte(),
            packet.readByte(),
            packet.readByte()
        )
    }

}