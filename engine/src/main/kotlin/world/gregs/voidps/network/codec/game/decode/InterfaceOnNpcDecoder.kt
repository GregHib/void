package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnNpcDecoder : Decoder(11) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnNPC(
            context = context,
            slot = packet.readShort(Modifier.ADD, Endian.LITTLE),
            hash = packet.readInt(),
            type = packet.readShort(order = Endian.LITTLE),
            run = packet.readBoolean(Modifier.ADD),
            npc = packet.readShort(Modifier.ADD)
        )
    }

}