package rs.dusk.network.rs.codec.service.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class UpdateHandshakeMessageDecoder : MessageDecoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateHandshake(
            context = context,
            version = packet.readInt()
        )
    }

}
