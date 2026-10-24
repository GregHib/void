package content.bot.behaviour.activity

import content.bot.Bot
import content.bot.behaviour.skill
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.random

/**
 * Makes bots more likely to stay in the same skill than bounce between unrelated activities,
 * by picking from the available activities which train the same skill as the [Bot.lastSuccessful] activity.
 * @param chance percentage (0 - 100) chance of favouring the same skill, otherwise defers to the next picker
 */
class SameSkillPicker(private val chance: () -> Int = { Settings["bots.sameSkillChance", 75] }) : ActivityPicker {
    override fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity? {
        val skill = bot.lastSuccessful?.skill ?: return null
        if (random.nextInt(100) >= chance()) {
            return null
        }
        return bot.available
            .mapNotNull { activities[it] }
            .filter { it.skill == skill && eligible(it) }
            .randomOrNull(random)
    }
}
