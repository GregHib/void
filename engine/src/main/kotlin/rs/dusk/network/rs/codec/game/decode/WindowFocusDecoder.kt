package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class WindowFocusDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.windowFocus(
            context = context,
            focused = packet.readBoolean()
        )
    }

}