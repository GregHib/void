package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class ObjectOption2Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            y = packet.readShortAddLittle(),
            x = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            objectId = packet.readShortAddLittle(),
            option = 2
        )
    }

}