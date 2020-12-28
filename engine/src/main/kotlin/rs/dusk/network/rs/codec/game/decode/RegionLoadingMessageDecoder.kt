package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class RegionLoadingMessageDecoder : MessageDecoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        //1057001181
        handler?.regionLoading(context)
    }

}