package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class DialogueContinueDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.continueDialogue(
            context,
            button = packet.readShort(Modifier.ADD),
            hash = packet.readInt(order = Endian.MIDDLE)
        )
    }

}