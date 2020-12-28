package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class WalkMapMessageDecoder : MessageDecoder(5) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.walk(
            context = context,
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(Modifier.ADD, Endian.LITTLE),
            running = packet.readBoolean()
        )
    }

}