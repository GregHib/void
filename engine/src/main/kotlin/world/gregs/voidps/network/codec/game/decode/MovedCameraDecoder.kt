package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class MovedCameraDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.cameraMoved(
            context,
            packet.readUnsignedShort(),
            packet.readUnsignedShort()
        )
    }

}