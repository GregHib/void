package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceOption(
            context,
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(),
            index
        )
    }

}