package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Compares the bot's *current* level for [skill] against its base/max as a percentage.
 * Used to gate reactive drinks on stat-debuff thresholds (e.g. drink super_restore once
 * strength has been brewed below 30% of max), independently of absolute level numbers.
 */
data class BotSkillPercent(val skill: Skill, val minPercent: Int? = null, val maxPercent: Int? = null) : Condition(1) {
    override fun keys() = setOf("skill:${skill.name.lowercase()}")
    override fun events() = setOf("skill:${skill.name.lowercase()}")
    override fun check(player: Player): Boolean {
        val maxLevel = player.levels.getMax(skill)
        if (maxLevel <= 0) return false
        val percent = player.levels.get(skill) * 100 / maxLevel
        return inRange(percent, minPercent, maxPercent)
    }
}
