package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceSwitch(
            context = context,
            fromHash = packet.readInt(),
            toSlot = packet.readShortLittle(),
            toHash = packet.readIntMiddle(),
            fromType = packet.readShort(),
            fromSlot = packet.readShortAddLittle(),
            toType = packet.readShortAddLittle()
        )
    }

}