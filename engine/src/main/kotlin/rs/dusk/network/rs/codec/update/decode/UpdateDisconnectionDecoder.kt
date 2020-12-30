package rs.dusk.network.rs.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class UpdateDisconnectionDecoder : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.updateDisconnect(
            context = context,
            id = packet.readUnsignedMedium()
        )
    }

}