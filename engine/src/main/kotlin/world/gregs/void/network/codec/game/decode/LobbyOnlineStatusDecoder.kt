package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class LobbyOnlineStatusDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.lobbyOnlineStatus(
            context,
            packet.readByte(),
            packet.readByte(),
            packet.readByte()
        )
    }

}