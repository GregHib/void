package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class PlayerOption3Decoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readByteSubtract()
        handler?.playerOption(
            context,
            index = packet.readShortLittle(),
            optionIndex = 3
        )
    }

}