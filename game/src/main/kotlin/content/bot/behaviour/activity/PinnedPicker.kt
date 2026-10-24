package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Bots with a [Bot.pinned] activity always do that activity, or idle if they can't.
 */
object PinnedPicker : ActivityPicker {
    override fun pick(bot: Bot, pool: ActivityPool): BotActivity? {
        val pinned = bot.pinned ?: return null
        val activity = pool.activity(pinned)
        return if (pool.hasRequirements(bot, activity)) activity else pool.idle
    }
}
