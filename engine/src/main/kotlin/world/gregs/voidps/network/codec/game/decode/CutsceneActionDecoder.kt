package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class CutsceneActionDecoder : Decoder(0) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.cutsceneAction(context)
    }

}