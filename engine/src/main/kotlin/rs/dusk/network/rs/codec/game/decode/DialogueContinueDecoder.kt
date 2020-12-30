package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class DialogueContinueDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.continueDialogue(
            context,
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
            packet.readShort(Modifier.ADD, Endian.LITTLE)
        )
    }

}