package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnFloorItem(
            context,
            packet.readShort(),
            packet.readShort(),
            packet.readShortAddLittle(),
            packet.readIntInverseMiddle(),
            packet.readShortLittle(),
            packet.readBoolean(),
            packet.readShortLittle()
        )
    }

}