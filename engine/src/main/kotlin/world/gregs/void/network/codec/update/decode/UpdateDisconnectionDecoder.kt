package world.gregs.void.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class UpdateDisconnectionDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateDisconnect(
            context = context,
            id = packet.readUnsignedMedium()
        )
    }

}