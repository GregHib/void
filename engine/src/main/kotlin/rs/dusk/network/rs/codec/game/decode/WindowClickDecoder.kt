package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class WindowClickDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.windowClick(
            context = context,
            hash = packet.readShort(Modifier.ADD, Endian.LITTLE),
            position = packet.readInt(order = Endian.MIDDLE)
        )
    }

}