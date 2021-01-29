package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceSwitch(
            context = context,
            fromHash = packet.readInt(),
            toType = packet.readShort(order = Endian.LITTLE),
            toHash = packet.readInt(order = Endian.MIDDLE),
            fromSlot = packet.readShort(),
            fromType = packet.readShort(Modifier.ADD, Endian.LITTLE),
            toSlot = packet.readShort(Modifier.ADD, Endian.LITTLE)
        )
    }

}