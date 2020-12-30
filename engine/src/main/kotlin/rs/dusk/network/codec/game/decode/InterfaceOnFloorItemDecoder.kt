package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class InterfaceOnFloorItemDecoder : Decoder(15) {

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