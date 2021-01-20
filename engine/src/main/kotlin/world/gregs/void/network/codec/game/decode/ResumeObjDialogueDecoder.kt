package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class ResumeObjDialogueDecoder : Decoder(2) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.resumeObjectDialogue(
            context = context,
            value = packet.readShort()
        )
    }

}