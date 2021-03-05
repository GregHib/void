package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class APCoordinateDecoder : Decoder(12) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.apCoordinate(
            context,
            packet.readShortAdd(),
            packet.readShortLittle(),
            packet.readUnsignedIntMiddle(),
            packet.readShortAdd(),
            packet.readShort()
        )
    }
}