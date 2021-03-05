package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class ObjectOption3Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            y = packet.readShortAdd(),
            objectId = packet.readShortAddLittle(),
            x = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            option = 3
        )
    }

}