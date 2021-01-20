package world.gregs.void.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class UpdateLoginStatusDecoder(private val online: Boolean) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateLoginStatus(
            context = context,
            online = online,
            value = packet.readUnsignedMedium()
        )
    }

}