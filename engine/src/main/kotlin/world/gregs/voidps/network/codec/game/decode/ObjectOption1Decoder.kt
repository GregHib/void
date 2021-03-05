package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class ObjectOption1Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            run = packet.readBooleanSubtract(),
            x = packet.readShortAddLittle(),
            y = packet.readShortLittle(),
            objectId = packet.readShort(),
            option = 1
        )
    }

}