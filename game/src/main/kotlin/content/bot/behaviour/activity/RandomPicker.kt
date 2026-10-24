package content.bot.behaviour.activity

import content.bot.Bot
import world.gregs.voidps.type.random

/**
 * Pick any [Bot.available] activity at random.
 */
object RandomPicker : ActivityPicker {
    override fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity? = bot.available
        .filter { eligible(activities[it]) }
        .randomOrNull(random)
        ?.let { activities[it] }
}
