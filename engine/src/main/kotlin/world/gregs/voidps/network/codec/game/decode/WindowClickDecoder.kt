package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader

class WindowClickDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.windowClick(
            context = context,
            hash = packet.readShort(Modifier.ADD, Endian.LITTLE),
            position = packet.readInt(order = Endian.MIDDLE)
        )
    }

}