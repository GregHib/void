package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Repeat the [Bot.previous] activity for as long as it remains possible.
 */
object RepeatPreviousPicker : ActivityPicker {
    override fun pick(bot: Bot, pool: ActivityPool): BotActivity? {
        val previous = bot.previous ?: return null
        return if (previous != pool.idle && pool.hasRequirements(bot, previous)) previous else null
    }
}
