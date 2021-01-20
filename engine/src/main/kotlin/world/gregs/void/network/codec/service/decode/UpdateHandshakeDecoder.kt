package world.gregs.void.network.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class UpdateHandshakeDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
