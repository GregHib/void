package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.buffer.read.Reader

class WindowFocusDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.windowFocus(
            context = context,
            focused = packet.readBoolean()
        )
    }

}