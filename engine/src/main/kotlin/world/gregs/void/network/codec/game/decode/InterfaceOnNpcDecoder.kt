package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class InterfaceOnNpcDecoder : Decoder(11) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnNPC(
            context,
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readBoolean()
        )
    }

}