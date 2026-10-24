package content.bot.behaviour.activity

import content.bot.Bot

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

val DEFAULT_PICKERS: List<ActivityPicker> = listOf(PinnedPicker, RepeatPreviousPicker, SameSkillPicker(), RandomPicker)
