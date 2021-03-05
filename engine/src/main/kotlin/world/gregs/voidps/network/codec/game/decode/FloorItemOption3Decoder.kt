package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption3Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            id = packet.readShort(),
            x = packet.readShortAdd(),
            run = packet.readBoolean(),
            y = packet.readShortAddLittle(),
            optionIndex = 2
        )
    }

}