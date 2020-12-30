package rs.dusk.network.rs.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class UpdateHandshakeDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
