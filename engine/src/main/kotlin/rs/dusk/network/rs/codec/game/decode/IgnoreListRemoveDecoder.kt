package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader
import rs.dusk.core.network.codec.packet.PacketSize.BYTE

class IgnoreListRemoveDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.removeIgnore(
            context,
            packet.readString()
        )
    }

}