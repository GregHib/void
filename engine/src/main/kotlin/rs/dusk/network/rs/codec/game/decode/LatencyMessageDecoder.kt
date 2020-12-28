package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class LatencyMessageDecoder : MessageDecoder(2) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.latency(
            context,
            packet.readShort()
        )
    }

}