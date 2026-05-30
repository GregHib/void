package content.bot.combat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Stub for wilderness roaming bots that target real players (PK fodder).
 *
 * Not registered yet — the wilderness implementation will land in a follow-up PR.
 * That PR is responsible for: combat-level band matching, per-zone density caps,
 * multi-combat awareness, real-player-only target selection, and the appropriate
 * cache tables / templates.
 */
class WildernessBotContext : CombatBotContext {

    override val id: String = "wilderness"

    override fun handles(tier: CombatTier): Boolean = tier.activityId.startsWith("wilderness_")

    override fun load() {
        // TODO(wilderness): load wilderness band tables.
    }

    override fun arenaKeys(): Set<String> = emptySet()

    override fun arenaSpawn(arenaKey: String): Tile? = null

    override fun arenaTiers(arenaKey: String): List<CombatTier> = emptyList()

    override fun autospawnIntervalTicks(arenaKey: String): Int = 0

    override fun autospawnTarget(arenaKey: String): Int = 0

    override fun arenaContains(arenaKey: String, tier: CombatTier): Boolean = false

    /** Wilderness drops: standard player-death rules apply (full kit drop). */
    override fun shouldDropItems(player: Player, tier: CombatTier): Boolean = true

    /** Wilderness has no central respawn — fall through to engine default. */
    override fun respawnTile(tier: CombatTier): Tile? = null
}
