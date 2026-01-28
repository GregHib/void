package content.bot.fact

import content.bot.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed class MandatoryFact(priority: Int = 0) : Fact(priority)

data class HasSkillLevel(
    val skill: Skill,
    val min: Int = 1,
    val max: Int = 120
) : MandatoryFact() {
    override fun check(bot: Bot) = bot.player.levels.get(skill) in min..max
}

data class HasVariable(
    val id: String,
    val value: Any? = null
) : MandatoryFact() {
    override fun check(bot: Bot) = bot.player.variables.get<Any>(id) == value
}
