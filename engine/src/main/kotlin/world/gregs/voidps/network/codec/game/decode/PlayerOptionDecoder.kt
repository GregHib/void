package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class PlayerOptionDecoder(private val index: Int) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readByte()//0
        handler?.playerOption(
            context,
            index = packet.readShort(),
            option = index + 1
        )
    }

}