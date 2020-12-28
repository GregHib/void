package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class InterfaceOnFloorItemMessageDecoder : MessageDecoder(15) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceOnFloorItem(
            context,
            packet.readShort(),
            packet.readShort(),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readBoolean(),
            packet.readShort(order = Endian.LITTLE)
        )
    }

}