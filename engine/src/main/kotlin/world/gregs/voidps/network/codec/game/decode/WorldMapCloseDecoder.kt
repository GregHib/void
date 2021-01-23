package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.buffer.read.Reader

class WorldMapCloseDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.closeWorldMap(context = context)
    }
}