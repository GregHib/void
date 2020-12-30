package rs.dusk.network.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class UpdateHandshakeDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
