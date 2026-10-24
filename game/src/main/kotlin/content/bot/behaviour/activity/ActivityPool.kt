package content.bot.behaviour.activity

import content.bot.Bot

/**
 * The activities a [ActivityPicker] can choose from
 */
interface ActivityPool {
    /**
     * Activity assigned when nothing else can be picked
     */
    val idle: BotActivity

    fun activity(id: String): BotActivity?

    /**
     * Whether [activity] has free slots, isn't blocked for [bot] and all of its requirements are met
     */
    fun hasRequirements(bot: Bot, activity: BotActivity?): Boolean
}
