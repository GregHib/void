package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class ObjectOption4Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            run = packet.readBooleanAdd(),
            objectId = packet.readShortAdd(),
            x = packet.readShortAdd(),
            y = packet.readShortLittle(),
            option = 4
        )
    }

}