package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeInt
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.SKILL_LEVEL

/**
 * @author GregHib <greg@gregs.world>
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
        writeByte(skill, Modifier.ADD)
        writeInt(experience, order = Endian.LITTLE)
    }
}