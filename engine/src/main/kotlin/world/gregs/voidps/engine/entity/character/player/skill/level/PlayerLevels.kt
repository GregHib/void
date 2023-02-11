package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import kotlin.math.pow

class PlayerLevels(
    private val experience: Experience
) : Levels.Level {

    override fun getMaxLevel(skill: Skill): Int {
        val exp = experience.get(skill)
        return getLevel(exp, skill)
    }

    companion object {
        fun getLevel(experience: Double, skill: Skill): Int {
            var total = 0
            for (level in 1..if (skill == Skill.Dungeoneering) 120 else 99) {
                total += experience(level)
                if (total / 4 - 1 >= experience) {
                    return if (skill == Skill.Constitution) level * 10 else level
                }
            }
            return if (skill == Skill.Constitution) 990 else 99
        }

        fun getExperience(level: Int, skill: Skill) = getExperience(if (skill == Skill.Constitution) level / 10 else level)

        private fun getExperience(level: Int): Double = (1 until level)
            .sumOf(Companion::experience) / 4.0

        @JvmStatic
        fun main(args: Array<String>) {
            println(getLevel(0.0, Skill.Constitution))
            println(getLevel(0.0, Skill.Magic))
            println(getExperience(1, Skill.Constitution))
            println(getExperience(1, Skill.Magic))
        }

        private fun experience(level: Int) = (level + 300.0 * 2.0.pow(level / 7.0)).toInt()
    }
}