package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.buffer.read.Reader

class WorldListRefreshDecoder : Decoder(4) {

    override fun decode(ctx: ChannelHandlerContext, packet: Reader) {
        handler?.refreshWorldList(
            context = ctx,
            full = packet.readInt() == 0
        )
    }

}