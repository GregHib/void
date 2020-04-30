package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CUTSCENE_ACTION
import rs.dusk.network.rs.codec.game.decode.message.CutsceneActionMessage

@PacketMetaData(opcodes = [CUTSCENE_ACTION], length = 0)
class CutsceneActionMessageDecoder : GameMessageDecoder<CutsceneActionMessage>() {

    override fun decode(packet: PacketReader) = CutsceneActionMessage

}