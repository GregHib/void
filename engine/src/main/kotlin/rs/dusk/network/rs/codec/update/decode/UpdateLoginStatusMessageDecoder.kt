package rs.dusk.network.rs.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class UpdateLoginStatusMessageDecoder(private val online: Boolean) : MessageDecoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateLoginStatus(
            context = context,
            online = online,
            value = packet.readUnsignedMedium()
        )
    }

}