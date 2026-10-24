package content.bot.behaviour.activity

import content.bot.Bot
import world.gregs.voidps.type.random

/**
 * Pick any [Bot.available] activity at random.
 */
object RandomPicker : ActivityPicker {
    override fun pick(bot: Bot, pool: ActivityPool): BotActivity? = bot.available
        .filter { pool.hasRequirements(bot, pool.activity(it)) }
        .randomOrNull(random)
        ?.let { pool.activity(it) }
}
