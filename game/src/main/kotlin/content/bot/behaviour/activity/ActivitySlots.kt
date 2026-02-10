package content.bot.behaviour.activity

/**
 * Track bots occupying and releasing access to a fixed number of slots per activity
 */
class ActivitySlots {
    private val occupied = mutableMapOf<String, Int>()

    fun hasFree(activity: BotActivity): Boolean = occupied.getOrDefault(activity.id, 0) < activity.capacity

    fun occupy(activity: BotActivity) {
        occupied[activity.id] = (occupied.getOrDefault(activity.id, 0) + 1).coerceAtMost(activity.capacity)
    }

    fun release(activity: BotActivity) {
        occupied[activity.id] = ((occupied[activity.id] ?: 1) - 1).coerceAtLeast(0)
    }
}
