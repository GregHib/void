package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class PlayerOption2Decoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readByte()
        handler?.playerOption(
            context,
            index = packet.readShort(),
            optionIndex = 2
        )
    }

}