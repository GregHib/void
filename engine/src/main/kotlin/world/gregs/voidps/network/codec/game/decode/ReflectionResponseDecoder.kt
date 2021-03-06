package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class ReflectionResponseDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readByte()//0
        handler?.reflectionResponse(context)
    }
}