package content.bot.behaviour.activity

import content.bot.Bot

/**
 * Chooses which [BotActivity] an idle [Bot] should do next.
 * Pickers are tried in order, the first to return a non-null activity wins.
 */
interface ActivityPicker {
    /**
     * @param activities every activity by id
     * @param idle activity assigned when nothing else can be done
     * @param eligible whether an activity has free slots, isn't blocked for the bot and has its requirements met
     * @return the activity to assign or null to defer to the next picker
     */
    fun pick(bot: Bot, activities: Map<String, BotActivity>, idle: BotActivity, eligible: (BotActivity?) -> Boolean): BotActivity?

    companion object {
        val DEFAULT_PICKERS: List<ActivityPicker> = listOf(PinnedPicker, RepeatPreviousPicker, SameSkillPicker, RandomPicker)
    }
}
