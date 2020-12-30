package rs.dusk.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class UpdateConnectionDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateConnection(
            context = context,
            id = packet.readUnsignedMedium()
        )
    }

}