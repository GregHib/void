package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption5Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            y = packet.readShort(),
            x = packet.readShort(Modifier.ADD),
            run = packet.readBoolean(Modifier.INVERSE),
            id = packet.readShort(Modifier.ADD),
            optionIndex = 4
        )
    }

}