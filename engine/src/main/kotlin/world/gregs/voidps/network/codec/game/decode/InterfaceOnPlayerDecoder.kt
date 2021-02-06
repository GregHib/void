package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnPlayer(
            context = context,
            player = packet.readShort(Modifier.ADD, Endian.LITTLE),
            type = packet.readShort(order = Endian.LITTLE),
            slot = packet.readShort(order = Endian.LITTLE),
            hash = packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            run = packet.readBoolean(Modifier.INVERSE)
        )
    }

}