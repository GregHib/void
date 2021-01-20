package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOption(
            context,
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(),
            index
        )
    }

}