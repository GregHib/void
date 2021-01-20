package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class NPCOptionDecoder(private val index: Int) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.npcOption(
            context,
            packet.readBoolean(Modifier.ADD),
            packet.readShort(Modifier.ADD),
            index + 1
        )
    }

}