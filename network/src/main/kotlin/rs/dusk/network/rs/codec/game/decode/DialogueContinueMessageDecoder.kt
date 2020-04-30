package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.DIALOGUE_CONTINUE
import rs.dusk.network.rs.codec.game.decode.message.DialogueContinueMessage

@PacketMetaData(opcodes = [DIALOGUE_CONTINUE], length = 6)
class DialogueContinueMessageDecoder : GameMessageDecoder<DialogueContinueMessage>() {

    override fun decode(packet: PacketReader) = DialogueContinueMessage(
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE)
    )

}