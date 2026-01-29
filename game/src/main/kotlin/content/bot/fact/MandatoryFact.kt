package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill

data class HasSkillLevel(
    val skill: Skill,
    val min: Int = 1,
    val max: Int = 120
) : Fact(0) {
    override fun check(bot: Bot) = bot.player.levels.get(skill) in min..max
}
