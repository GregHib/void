package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOption(
            context,
            packet.readInt(),
            packet.readShort(),
            packet.readShort(),
            index
        )
    }

}