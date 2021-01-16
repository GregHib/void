package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeInt
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.SKILL_LEVEL

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class SkillLevelEncoder : Encoder(SKILL_LEVEL) {

    /**
     * Updates the players skill level & experience
     * @param skill The skills id
     * @param level The current players level
     * @param experience The current players experience
     */
    fun encode(
        player: Player,
        skill: Int,
        level: Int,
        experience: Int
    ) = player.send(6) {
        writeByte(level, Modifier.SUBTRACT)
        writeByte(skill, Modifier.INVERSE)
        writeInt(experience, Modifier.INVERSE)
    }
}