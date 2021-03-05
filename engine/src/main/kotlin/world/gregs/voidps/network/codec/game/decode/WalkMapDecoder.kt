package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class WalkMapDecoder : Decoder(5) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.walk(
            context = context,
            y = packet.readShortLittle(),
            running = packet.readBooleanAdd(),
            x = packet.readShortAdd()
        )
    }

}