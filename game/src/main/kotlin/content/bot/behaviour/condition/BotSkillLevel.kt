package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

data class BotSkillLevel(val skill: Skill, val min: Int? = null, val max: Int? = null) : Condition(1) {
    override fun keys() = setOf("skill:${skill.name.lowercase()}")
    override fun events() = setOf("skill:${skill.name.lowercase()}")
    // Base/max level (XP-driven), not current — otherwise a drained Prayer or HP drop knocks
    // the bot out of its pinned activity's `requires` check and stalls it in idle until those
    // stats naturally regenerate.
    override fun check(player: Player) = inRange(player.levels.getMax(skill), min, max)
}
