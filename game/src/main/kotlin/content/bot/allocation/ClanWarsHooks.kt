package content.bot.allocation

import com.github.michaelbull.logging.InlineLogger
import content.bot.Bot
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Lifecycle rules for the Clan Wars free-for-all allocations.
 * - Bots respawn back at the Clan Wars portals.
 * - Only [dangerous] arena bots drop their items on death.
 * - [dangerous] arena bots which teleport out to retreat are restocked before walking back in.
 */
class ClanWarsHooks(private val dangerous: Boolean) : AllocationHooks {
    private val logger = InlineLogger("ClanWarsBots")

    override val subscribedAreas: Set<String> = if (dangerous) setOf(TELEPORT) else emptySet()

    override fun shouldDropItems(player: Player): Boolean = dangerous || player.tile in Areas["clan_wars_ffa_dangerous_arena"]

    override fun respawnTile(bot: Bot): Tile = Areas[TELEPORT].random()

    override fun onAreaEntered(bot: Bot, area: String) {
        if (dangerous && area == TELEPORT) {
            logger.info { "PvP bot retreat: '${bot.player.accountName}' role=${bot.role} teleported to $TELEPORT" }
        }
    }

    override fun shouldRefreshOnAreaEntered(bot: Bot, area: String): Boolean = dangerous && area == TELEPORT

    companion object {
        const val SAFE = "clan_wars_ffa_safe"
        const val DANGEROUS = "clan_wars_ffa_dangerous"
        private const val TELEPORT = "clan_wars_teleport"
    }
}
