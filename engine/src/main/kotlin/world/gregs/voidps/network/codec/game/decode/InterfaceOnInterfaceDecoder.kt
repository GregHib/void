package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnInterface(
            context = context,
            fromHash = packet.readInt(),
            toHash = packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            fromItem = packet.readShort(Modifier.ADD),
            from = packet.readShort(),
            toItem = packet.readShort(Modifier.ADD),
            to = packet.readShort()
        )
    }

}