package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.RESUME_PLAYER_OBJ_DIALOGUE
import rs.dusk.network.rs.codec.game.decode.message.ResumeObjDialogueMessage

@PacketMetaData(opcodes = [RESUME_PLAYER_OBJ_DIALOGUE], length = 2)
class ResumeObjDialogueMessageDecoder : GameMessageDecoder<ResumeObjDialogueMessage>() {

    override fun decode(packet: PacketReader) = ResumeObjDialogueMessage(packet.readShort())

}