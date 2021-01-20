package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Decoder
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader

class WindowClickDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.windowClick(
            context = context,
            hash = packet.readShort(Modifier.ADD, Endian.LITTLE),
            position = packet.readInt(order = Endian.MIDDLE)
        )
    }

}