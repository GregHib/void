package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class RegionLoadingDecoder : Decoder(4) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        //1057001181
        handler?.regionLoading(context)
    }

}