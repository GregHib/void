package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.DialogueContinueMessage

class DialogueContinueMessageDecoder : MessageDecoder<DialogueContinueMessage>(6) {

    override fun decode(packet: PacketReader) = DialogueContinueMessage(
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE)
    )

}