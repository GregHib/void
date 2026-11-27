package content.bot.allocation

import content.bot.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Minigame specific lifecycle rules for bots in an allocation, registered in Kotlin by allocation id with [BotAllocations.register].
 */
interface AllocationHooks {
    /**
     * Areas whose `entered` event should be passed to [onAreaEntered]
     */
    val subscribedAreas: Set<String>
        get() = emptySet()

    /**
     * Whether a dying bot drops its items
     */
    fun shouldDropItems(player: Player): Boolean = true

    /**
     * Where a dead bot respawns, null for the default respawn location
     */
    fun respawnTile(bot: Bot): Tile? = null

    fun onAreaEntered(bot: Bot, area: String) {}

    /**
     * Whether a role bot should be re-provisioned after entering [area] (e.g. restocking after teleporting out)
     */
    fun shouldRefreshOnAreaEntered(bot: Bot, area: String): Boolean = false

    object Default : AllocationHooks
}
