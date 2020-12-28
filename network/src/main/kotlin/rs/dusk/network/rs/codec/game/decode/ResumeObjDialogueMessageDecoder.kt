package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.ResumeObjDialogueMessage

class ResumeObjDialogueMessageDecoder : GameMessageDecoder<ResumeObjDialogueMessage>(2) {

    override fun decode(packet: PacketReader) = ResumeObjDialogueMessage(packet.readShort())

}