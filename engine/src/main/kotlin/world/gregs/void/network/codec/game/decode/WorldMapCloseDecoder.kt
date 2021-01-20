package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Decoder
import world.gregs.void.buffer.read.Reader

class WorldMapCloseDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.closeWorldMap(context = context)
    }
}