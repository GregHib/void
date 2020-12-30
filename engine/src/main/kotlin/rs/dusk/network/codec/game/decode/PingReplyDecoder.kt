package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class PingReplyDecoder : Decoder(8) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.pingReply(
            context,
            packet.readInt(),
            packet.readInt()
        )
    }

}