package content.bot.behaviour.activity

import world.gregs.voidps.engine.data.definition.Areas

/**
 * Track bots occupying and releasing access to a fixed number of slots per area.
 * An area's limit is its `bot_capacity` parameter, areas without one are unlimited.
 * Activities without an area never use up slots.
 */
class ActivitySlots {
    private val occupied = mutableMapOf<String, Int>()

    /**
     * Whether [activity] has an area with a free slot (or the area it is bound to does).
     */
    fun hasFree(activity: BotActivity): Boolean {
        val bound = activity.boundArea
        if (bound != null) {
            return hasFree(bound)
        }
        val area = activity.area ?: return true
        return area.areas().any { hasFree(it) }
    }

    fun hasFree(area: String): Boolean {
        val capacity = capacity(area) ?: return true
        return occupied.getOrDefault(area, 0) < capacity
    }

    fun occupy(activity: BotActivity) {
        val area = activity.boundArea ?: return
        occupied[area] = occupied.getOrDefault(area, 0) + 1
    }

    fun release(activity: BotActivity) {
        val area = activity.boundArea ?: return
        occupied[area] = ((occupied[area] ?: 1) - 1).coerceAtLeast(0)
    }

    companion object {
        fun capacity(area: String): Int? = Areas.getOrNull(area)?.getOrNull<Int>("bot_capacity")
    }
}
