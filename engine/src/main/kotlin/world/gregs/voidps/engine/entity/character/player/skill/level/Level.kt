package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation.interpolate
import world.gregs.voidps.type.random

object Level {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 99
    private const val MAX_CHANCE = 256

    val SUCCESS = MAX_CHANCE..MAX_CHANCE

    /**
     * Calculates random chance of being successful
     * @param level The players current level
     * @param chances The chance rates (out of [MAX_CHANCE]) at level 1 and 99
     * @return success
     */
    fun success(level: Int, chances: IntRange): Boolean {
        val chance = chance(level, chances)
        val random = random.nextInt(MAX_CHANCE)
        return chance > random
    }

    /**
     * The chance of being successful (out of [MAX_CHANCE])
     */
    private fun chance(level: Int, chances: IntRange): Int {
        return interpolate(level.coerceIn(MIN_LEVEL, MAX_LEVEL), chances.first, chances.last, MIN_LEVEL, MAX_LEVEL)
    }

    fun Player.has(skill: Skill, level: Int, message: Boolean = false): Boolean {
        if (levels.get(skill) < level) {
            if (message) {
                message("You need to have${skill.name.an()} ${skill.name} level of ${if (skill == Skill.Constitution) level / 10 else level}.")
            }
            return false
        }
        return true
    }

    fun Player.hasMax(skill: Skill, level: Int, message: Boolean = false): Boolean {
        if (levels.getMax(skill) < level) {
            if (message) {
                message("You need to have${skill.name.an()} ${skill.name} level of ${if (skill == Skill.Constitution) level / 10 else level}.")
            }
            return false
        }
        return true
    }

}