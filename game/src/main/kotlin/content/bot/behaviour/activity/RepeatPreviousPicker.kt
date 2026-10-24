package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Repeat the [Bot.previous] activity for as long as it remains possible.
 */
object RepeatPreviousPicker : ActivityPicker {
    override fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity? {
        val previous = bot.previous ?: return null
        return if (previous != idle && eligible(previous)) previous else null
    }
}
