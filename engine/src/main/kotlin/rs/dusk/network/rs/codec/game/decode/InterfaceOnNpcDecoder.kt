package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class InterfaceOnNpcDecoder : Decoder(11) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceOnNPC(
            context,
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readBoolean()
        )
    }

}