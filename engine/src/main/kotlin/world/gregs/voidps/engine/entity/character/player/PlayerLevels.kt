package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.pow

class PlayerLevels(
    private val experience: Experience
) : Levels.Level {

    override fun getMaxLevel(skill: Skill): Int {
        val exp = experience.get(skill)
        return if (skill == Skill.Constitution) getLevel(exp) * 10 else getLevel(exp, skill == Skill.Dungeoneering)
    }

    companion object {
        fun getLevel(experience: Double, oneTwenty: Boolean = false): Int {
            var total = 0
            return (1..if (oneTwenty) 120 else 99).firstOrNull { level ->
                total += experience(level)
                total / 4 - 1 >= experience
            } ?: 99
        }

        fun getExperience(level: Int): Int = (1 until level)
            .sumBy(Companion::experience) / 4

        private fun experience(level: Int) = (level + 300.0 * 2.0.pow(level / 7.0)).toInt()

        fun createLevels(experience: Experience): IntArray {
            return Skill.all.map { skill -> getLevel(experience.get(skill)) }.toIntArray()
        }
    }
}