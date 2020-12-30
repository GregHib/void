package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class InterfaceOnObjectDecoder : Decoder(15) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceOnObject(
            context,
            packet.readBoolean(Modifier.INVERSE),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(order = Endian.LITTLE),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt()
        )
    }

}