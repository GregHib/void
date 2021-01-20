package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceSwitch(
            context,
            packet.readShort(),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(Modifier.ADD),
            packet.readInt(order = Endian.MIDDLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE)
        )
    }

}