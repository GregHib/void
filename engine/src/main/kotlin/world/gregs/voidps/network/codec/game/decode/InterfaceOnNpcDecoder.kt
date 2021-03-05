package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnNpcDecoder : Decoder(11) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnNPC(
            context = context,
            slot = packet.readShortAddLittle(),
            hash = packet.readInt(),
            type = packet.readShortLittle(),
            run = packet.readBooleanAdd(),
            npc = packet.readShortAdd()
        )
    }

}