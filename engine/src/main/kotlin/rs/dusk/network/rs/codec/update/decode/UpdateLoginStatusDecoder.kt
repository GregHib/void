package rs.dusk.network.rs.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class UpdateLoginStatusDecoder(private val online: Boolean) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateLoginStatus(
            context = context,
            online = online,
            value = packet.readUnsignedMedium()
        )
    }

}