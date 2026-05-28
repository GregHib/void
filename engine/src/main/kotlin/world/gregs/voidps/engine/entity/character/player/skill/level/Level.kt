package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation.interpolate
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.random
import kotlin.math.pow

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
     * Calculates random chance of being successful
     * @param level The players current level
     * @param maxLevel The maximum level to fail at
     * @param minChance The chance rate (out of [MAX_CHANCE]) at level 1
     * @return success
     */
    fun success(level: Int, maxLevel: Int, minChance: Int = 1): Boolean {
        val chance = chance(level, minChance..MAX_CHANCE, maxLevel)
        val random = random.nextInt(MAX_CHANCE)
        return chance > random
    }

    /**
     * The chance of being successful (out of [MAX_CHANCE])
     */
    private fun chance(level: Int, chances: IntRange, maxLevel: Int = MAX_LEVEL): Int = interpolate(level.coerceIn(MIN_LEVEL, MAX_LEVEL), chances.first, chances.last, MIN_LEVEL, maxLevel)

    fun experience(skill: Skill, level: Int) = experience(if (skill == Skill.Constitution) level / 10 else level)

    fun experience(level: Int): Double = (1 until level)
        .sumOf(::experienceAt) / 4.0

    fun experienceAt(level: Int) = (level + 300.0 * 2.0.pow(level / 7.0)).toInt()

    fun Player.has(skill: Skill, level: Int, message: Boolean): Boolean = has(skill, level, if (message) "" else null)

    fun Player.has(skill: Skill, level: Int, message: String? = null): Boolean {
        if (levels.get(skill) < level) {
            if (message != null) {
                message("You need to have${skill.name.an()} ${skill.name} level of ${if (skill == Skill.Constitution) level / 10 else level}$message.")
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

    fun Player.hasRequirementsToUse(item: Item, message: Boolean = false, skills: Set<Skill> = emptySet()): Boolean {
        val def = item.def
        for (i in 1..6) {
            val index: Int = def.getOrNull("use_skill_${i}") ?: break
            val level: Int = def.getOrNull("use_level_${i}") ?: break
            val skill = Skill.all[index]
            if ((skills.isEmpty() || skills.contains(skill)) && !has(skill, level, message)) {
                return false
            }
        }
        return true
    }

    fun Player.hasRequirements(item: Item, message: Boolean = false): Boolean {
        val def = item.def
        for (i in 1..6) {
            val index: Int = def.getOrNull("equip_skill_${i}") ?: break
            val level: Int = def.getOrNull("equip_level_${i}") ?: break
            val skill = Skill.all[index]
            if (!hasMax(skill, level, message)) {
                return false
            }
        }
        val index: Int? = item.def.getOrNull(Params.SKILLCAPE_SKILL)
        if (index != null) {
            val skill = Skill.all[index]
            val max = if (skill == Skill.Dungeoneering && item.id.startsWith("dungeoneering_cape")) MAX_LEVEL else skill.maximum()
            if (!hasMax(skill, max, message)) {
                return false
            }
        }
        return appearance.combatLevel >= item.def[Params.COMBAT_REQ, 0]
    }

    fun Player.hasUseLevel(skill: Skill, item: Item, message: Boolean = false): Boolean {
        val level: Int = item.def.getOrNull("secondary_use_level") ?: return true
        return has(skill, level, message)
    }
}
