package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class WalkMapDecoder : Decoder(5) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.walk(
            context = context,
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(Modifier.ADD, Endian.LITTLE),
            running = packet.readBoolean()
        )
    }

}