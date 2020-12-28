package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.SkillcapeColourMessage

class SkillcapeColourMessageDecoder : GameMessageDecoder<SkillcapeColourMessage>(2) {

    override fun decode(packet: PacketReader) = SkillcapeColourMessage(packet.readUnsignedShort())

}