package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.SKILL_LEVEL
import rs.dusk.network.rs.codec.game.encode.message.SkillLevelMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class SkillLevelMessageEncoder : MessageEncoder<SkillLevelMessage> {

    override fun encode(builder: PacketWriter, msg: SkillLevelMessage) {
        val (skill, level, experience) = msg
        builder.apply {
            writeOpcode(SKILL_LEVEL)
            writeByte(level, Modifier.SUBTRACT)
            writeByte(skill, Modifier.ADD)
            writeInt(experience, order = Endian.LITTLE)
        }
    }
}