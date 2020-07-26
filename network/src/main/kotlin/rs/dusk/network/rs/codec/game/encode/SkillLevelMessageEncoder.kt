package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.SKILL_LEVEL
import rs.dusk.network.rs.codec.game.encode.message.SkillLevelMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class SkillLevelMessageEncoder : GameMessageEncoder<SkillLevelMessage>() {

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