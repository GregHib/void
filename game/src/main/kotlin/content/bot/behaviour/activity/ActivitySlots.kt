package content.bot.behaviour.activity

import world.gregs.voidps.engine.data.definition.Areas

/**
 * Track bots occupying and releasing access to a fixed number of slots per area.
 * An area's limit is its `bot_capacity` parameter, areas without one are unlimited.
 * Activities without an area never use up slots.
 *
 * World bots [occupy] and [release] slots as they take up and finish activities; companions [reserve] one for
 * as long as they're on a task, one per bot account, in the same pool.
 */
class ActivitySlots {
    private val occupied = mutableMapOf<String, Int>()

    /** Companion bot account to the area it holds a slot in. */
    private val reserved = mutableMapOf<String, String>()

    /** How many companions hold a slot in each area, kept beside [reserved] so [hasFree] is a map read. */
    private val reservedCount = mutableMapOf<String, Int>()

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
        return occupied(area) < capacity
    }

    /** The bots, world and companion, occupying [area] right now. */
    fun occupied(area: String): Int = worldOccupied(area) + reserved(area)

    /** The world bots occupying [area] right now. */
    fun worldOccupied(area: String): Int = occupied.getOrDefault(area, 0)

    /** The companions holding a slot in [area] right now. */
    fun reserved(area: String): Int = reservedCount.getOrDefault(area, 0)

    /** The area the companion [account] holds a slot in, if any. */
    fun reservedBy(account: String): String? = reserved[account]

    /** Hold a slot in [area] for the companion [account], giving up any it held before. */
    fun reserve(account: String, area: String) {
        unreserve(account)
        reserved[account] = area
        reservedCount[area] = reserved(area) + 1
    }

    /** Give up the slot the companion [account] holds, if any. */
    fun unreserve(account: String) {
        val area = reserved.remove(account) ?: return
        val count = reserved(area) - 1
        if (count <= 0) reservedCount.remove(area) else reservedCount[area] = count
    }

    fun occupy(activity: BotActivity) {
        val area = activity.boundArea ?: return
        occupied[area] = occupied.getOrDefault(area, 0) + 1
    }

    fun release(activity: BotActivity) {
        val area = activity.boundArea ?: return
        occupied[area] = ((occupied[area] ?: 1) - 1).coerceAtLeast(0)
    }

    /** Free every slot, world and companion. */
    fun clear() {
        occupied.clear()
        reserved.clear()
        reservedCount.clear()
    }

    companion object {
        fun capacity(area: String): Int? = Areas.getOrNull(area)?.getOrNull<Int>("bot_capacity")
    }
}
