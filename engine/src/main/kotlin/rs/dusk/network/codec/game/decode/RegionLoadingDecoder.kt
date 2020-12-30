package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        //1057001181
        handler?.regionLoading(context)
    }

}