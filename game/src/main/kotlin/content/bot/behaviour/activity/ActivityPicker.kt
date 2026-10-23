package content.bot.behaviour.activity

import content.bot.Bot
import world.gregs.voidps.type.random

/**
 * Chooses which [BotActivity] an idle [Bot] should do next.
 * Pickers are tried in order, the first to return a non-null activity wins.
 */
fun interface ActivityPicker {
    /**
     * @return the activity to assign or null to defer to the next picker
     */
    fun pick(bot: Bot, pool: ActivityPool): BotActivity?
}

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

/**
 * Repeat the [Bot.previous] activity for as long as it remains possible.
 */
object RepeatPreviousPicker : ActivityPicker {
    override fun pick(bot: Bot, pool: ActivityPool): BotActivity? {
        val previous = bot.previous ?: return null
        return if (previous != pool.idle && pool.hasRequirements(bot, previous)) previous else null
    }
}

/**
 * Pick any [Bot.available] activity at random.
 */
object RandomPicker : ActivityPicker {
    override fun pick(bot: Bot, pool: ActivityPool): BotActivity? = bot.available
        .filter { pool.hasRequirements(bot, pool.activity(it)) }
        .randomOrNull(random)
        ?.let { pool.activity(it) }
}

val DEFAULT_PICKERS: List<ActivityPicker> = listOf(PinnedPicker, RepeatPreviousPicker, RandomPicker)
