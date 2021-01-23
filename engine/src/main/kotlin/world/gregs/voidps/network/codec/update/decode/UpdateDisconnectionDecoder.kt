package world.gregs.voidps.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class UpdateDisconnectionDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateDisconnect(
            context = context,
            id = packet.readUnsignedMedium()
        )
    }

}