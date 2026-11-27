package content.bot.allocation

import content.bot.Bot
import world.gregs.voidps.engine.data.Settings

/**
 * Strategy for how many bots spawn into a part of the world and which role each is given.
 */
sealed class BotAllocator(val config: AllocationConfig) {
    val id: String
        get() = config.id

    val count: Int
        get() = config.countSetting?.let { Settings[it, config.count] } ?: config.count

    val spawnSeconds: Int
        get() = config.spawnSecondsSetting?.let { Settings[it, config.spawnSeconds] } ?: config.spawnSeconds

    /**
     * The role a newly spawned bot should be given considering the current [members], or null to let it pick activities freely.
     */
    abstract fun allocate(members: List<Bot>): BotRole?

    open fun role(id: String): BotRole? = null
}
