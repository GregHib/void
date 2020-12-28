package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class SecondaryTeleportMessageDecoder : MessageDecoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.secondaryTeleport(
            context = context,
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(order = Endian.LITTLE)
        )
    }

}