package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnPlayer(
            context,
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(order = Endian.LITTLE),
            packet.readShort(),
            packet.readBoolean(Modifier.SUBTRACT),
            packet.readShort(Modifier.ADD, Endian.LITTLE)
        )
    }

}