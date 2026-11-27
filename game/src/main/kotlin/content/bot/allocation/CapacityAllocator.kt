package content.bot.allocation

import content.bot.Bot

/**
 * Bots pick from any available activity, limited only by the capacity of each activity's area.
 */
class CapacityAllocator(config: AllocationConfig) : BotAllocator(config) {
    override fun allocate(members: List<Bot>): BotRole? = null
}