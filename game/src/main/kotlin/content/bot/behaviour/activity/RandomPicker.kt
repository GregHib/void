package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Pick any [Bot.available] activity at random, weighted towards those with [BotActivity.prefer] conditions met.
 */
object RandomPicker : ActivityPicker {
    override fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity? = ActivityPicker.weightedPick(
        bot,
        bot.available.mapNotNull { activities[it] }.filter { eligible(it) },
    )
}
