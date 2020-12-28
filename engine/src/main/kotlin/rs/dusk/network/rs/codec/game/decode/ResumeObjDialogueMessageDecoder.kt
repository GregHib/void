package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class ResumeObjDialogueMessageDecoder : MessageDecoder(2) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.resumeObjectDialogue(
            context = context,
            value = packet.readShort()
        )
    }

}