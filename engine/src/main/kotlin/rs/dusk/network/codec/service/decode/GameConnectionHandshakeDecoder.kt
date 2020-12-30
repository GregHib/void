package rs.dusk.network.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class GameConnectionHandshakeDecoder : Decoder(0) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.gameHandshake(context)
    }

}