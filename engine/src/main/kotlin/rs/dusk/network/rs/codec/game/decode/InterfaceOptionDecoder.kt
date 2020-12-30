package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

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