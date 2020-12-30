package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class WindowClickDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.windowClick(
            context = context,
            hash = packet.readShort(Modifier.ADD, Endian.LITTLE),
            position = packet.readInt(order = Endian.MIDDLE)
        )
    }

}