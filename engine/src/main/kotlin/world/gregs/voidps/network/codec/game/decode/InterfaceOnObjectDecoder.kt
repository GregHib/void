package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnObjectDecoder : Decoder(15) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnObject(
            context = context,
            y = packet.readShortAdd(),
            slot = packet.readShortAddLittle(),
            hash = packet.readIntLittle(),
            type = packet.readShortAdd(),
            run = packet.readBooleanSubtract(),
            x = packet.readShortLittle(),
            id = packet.readUnsignedShortLittle()
        )
    }

}