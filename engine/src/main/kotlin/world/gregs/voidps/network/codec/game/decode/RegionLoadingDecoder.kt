package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readInt()//1057001181
        handler?.regionLoading(context)
    }

}