package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption4Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAdd(),
            y = packet.readShortLittle(),
            id = packet.readShort(),
            optionIndex = 3
        )
    }

}