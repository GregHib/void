package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class FloorItemOption3Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context = context,
            id = packet.readShort(),
            x = packet.readShort(Modifier.ADD),
            run = packet.readBoolean(),
            y = packet.readShort(Modifier.ADD, Endian.LITTLE),
            optionIndex = 2
        )
    }

}