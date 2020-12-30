package rs.dusk.network.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class UpdateHandshakeDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
