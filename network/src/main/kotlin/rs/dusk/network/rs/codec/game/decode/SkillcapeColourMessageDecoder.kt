package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.COLOUR_ID
import rs.dusk.network.rs.codec.game.decode.message.SkillcapeColourMessage

@PacketMetaData(opcodes = [COLOUR_ID], length = 2)
class SkillcapeColourMessageDecoder : GameMessageDecoder<SkillcapeColourMessage>() {

    override fun decode(packet: PacketReader) = SkillcapeColourMessage(packet.readUnsignedShort())

}