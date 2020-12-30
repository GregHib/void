package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class WindowFocusDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.windowFocus(
            context = context,
            focused = packet.readBoolean()
        )
    }

}