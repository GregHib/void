package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader
import rs.dusk.core.network.codec.packet.PacketSize.BYTE

class ReflectionResponseDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        packet.readByte()//0
        handler?.reflectionResponse(context)
    }
}