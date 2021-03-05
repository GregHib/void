package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption5Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            y = packet.readShort(),
            x = packet.readShortAdd(),
            run = packet.readBooleanInverse(),
            id = packet.readShortAdd(),
            optionIndex = 4
        )
    }

}