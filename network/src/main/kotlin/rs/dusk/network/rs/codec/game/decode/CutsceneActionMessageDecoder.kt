package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.CutsceneActionMessage

class CutsceneActionMessageDecoder : GameMessageDecoder<CutsceneActionMessage>(0) {

    override fun decode(packet: PacketReader) = CutsceneActionMessage

}