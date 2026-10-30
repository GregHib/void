package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Bots with a [Bot.pinned] activity always do that activity, or idle if they can't.
 */
object PinnedPicker : ActivityPicker {
    override fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity? {
        val pinned = bot.pinned ?: return null
        val activity = activities[pinned]
        return if (eligible(activity)) activity else idle
    }
}
