package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

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