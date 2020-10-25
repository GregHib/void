package rs.dusk.engine.entity.character.player.skill

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import kotlin.random.Random

object Level {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 99
    private const val MAX_CHANCE = 256

    /**
     * Calculates random chance of being successful
     * @param level The players current level
     * @param chances The chance rates (out of [MAX_CHANCE]) at level 1 and 99
     * @return success
     */
    fun success(level: Int, chances: IntRange): Boolean {
        val chance = getChance(level, chances)
        val random = Random.nextInt(MAX_CHANCE)
        return chance > random
    }

    /**
     * The chance of being successful (out of [MAX_CHANCE])
     */
    fun getChance(level: Int, chances: IntRange): Int {
        return interpolate(level.coerceIn(MIN_LEVEL, MAX_LEVEL), chances.first, chances.last, MIN_LEVEL, MAX_LEVEL)
    }

    /**
     * Interpolates between two known points
     * https://en.wikipedia.org/wiki/Linear_interpolation
     */
    fun interpolate(x: Int, y1: Int, y2: Int, x1: Int, x2: Int): Int {
        return (y1 * (x2 - x) + y2 * (x - x1)) / (x2 - x1)
    }

    fun Player.has(skill: Skill, level: Int, message: Boolean): Boolean {
        if (levels.get(skill) < level) {
            if (message) {
                message("You need to have an ${skill.name} level of $level.")
            }
            return false
        }
        return true
    }

}