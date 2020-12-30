package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

class PingReplyDecoder : Decoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.pingReply(
            context,
            packet.readInt(),
            packet.readInt()
        )
    }

}