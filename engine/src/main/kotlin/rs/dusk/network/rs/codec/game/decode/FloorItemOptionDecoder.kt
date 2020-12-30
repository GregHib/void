package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class FloorItemOptionDecoder(private val index: Int) : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.floorItemOption(
            context,
            packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt(),
            packet.readBoolean(),
            packet.readShort(),
            packet.readShort(order = Endian.LITTLE),
            index
        )
    }

}