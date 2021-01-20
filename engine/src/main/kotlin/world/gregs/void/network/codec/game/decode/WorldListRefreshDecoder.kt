package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Decoder
import world.gregs.void.buffer.read.Reader

class WorldListRefreshDecoder : Decoder(4) {

    override fun decode(ctx: ChannelHandlerContext, packet: Reader) {
        handler?.refreshWorldList(
            context = ctx,
            full = packet.readInt() == 0
        )
    }

}