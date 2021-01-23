package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class SkillCapeColourDecoder : Decoder(2) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.skillCapeColour(
            context = context,
            colour = packet.readUnsignedShort()
        )
    }

}