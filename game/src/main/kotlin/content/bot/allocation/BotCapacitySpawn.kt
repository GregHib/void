package content.bot.allocation

import content.bot.Bot

/**
 * Bots pick from any available activity, limited only by the capacity of each activity's area.
 */
class BotCapacitySpawn(config: BotSpawnConfig) : BotSpawnStrategy(config) {
    override fun allocate(members: List<Bot>): BotRole? = null
}
