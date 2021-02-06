package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class PlayerOption5Decoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.playerOption(
            context,
            index = packet.readShort(Modifier.ADD),
            optionIndex = 5
        )
        packet.readByte()
    }

}