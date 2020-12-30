package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketSize.BYTE

class IgnoreListAddDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.addIgnore(
            context,
            packet.readString(),
            packet.readBoolean()
        )
    }

}