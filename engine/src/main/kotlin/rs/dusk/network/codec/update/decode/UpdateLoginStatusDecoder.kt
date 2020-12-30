package rs.dusk.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class UpdateLoginStatusDecoder(private val online: Boolean) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateLoginStatus(
            context = context,
            online = online,
            value = packet.readUnsignedMedium()
        )
    }

}