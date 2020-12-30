package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class WorldListRefreshDecoder : Decoder(4) {

    override fun decode(ctx: ChannelHandlerContext, packet: PacketReader) {
        handler?.refreshWorldList(
            context = ctx,
            full = packet.readInt() == 0
        )
    }

}