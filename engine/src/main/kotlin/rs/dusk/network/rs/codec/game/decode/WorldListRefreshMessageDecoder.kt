package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class WorldListRefreshMessageDecoder : MessageDecoder(4) {

    override fun decode(ctx: ChannelHandlerContext, packet: PacketReader) {
        handler?.refreshWorldList(
            context = ctx,
            full = packet.readInt() == 0
        )
    }

}