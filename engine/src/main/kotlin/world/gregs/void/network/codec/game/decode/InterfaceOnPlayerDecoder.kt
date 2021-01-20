package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

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