package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        //1057001181
        handler?.regionLoading(context)
    }

}