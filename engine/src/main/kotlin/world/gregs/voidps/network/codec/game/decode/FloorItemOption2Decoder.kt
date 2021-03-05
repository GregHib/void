package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption2Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            y = packet.readShortAdd(),
            id = packet.readShortAdd(),
            x = packet.readShortLittle(),
            run = packet.readBooleanInverse(),
            optionIndex = 1
        )
    }

}