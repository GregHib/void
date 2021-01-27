package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class WalkMapDecoder : Decoder(5) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.walk(
            context = context,
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(),
            running = packet.readBoolean()
        )
    }

}