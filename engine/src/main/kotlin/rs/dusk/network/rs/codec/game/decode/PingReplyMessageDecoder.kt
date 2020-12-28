package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class PingReplyMessageDecoder : MessageDecoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.pingReply(
            context,
            packet.readInt(),
            packet.readInt()
        )
    }

}