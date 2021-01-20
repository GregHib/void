package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnInterface(
            context,
            packet.readInt(order = Endian.MIDDLE),
            packet.readShort(Modifier.ADD),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readShort(Modifier.ADD),
            packet.readShort(order = Endian.LITTLE)
        )
    }

}