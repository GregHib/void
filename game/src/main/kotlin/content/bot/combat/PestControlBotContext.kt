package content.bot.combat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Stub for pest control PvE minigame bots (NPC combat, defender role, wave timing).
 *
 * Not registered yet — the pest control implementation will land in a follow-up PR. That PR
 * is responsible for: BotFightNpc-based combat, portal/defender role assignment, wave timing,
 * and any minigame-specific drop / respawn semantics.
 */
class PestControlBotContext : CombatBotContext {

    override val id: String = "pest_control"

    override fun handles(tier: CombatTier): Boolean = tier.activityId.startsWith("pest_control_")

    override fun load() {
        // TODO(pest_control): load wave / role tables.
    }

    override fun arenaKeys(): Set<String> = emptySet()

    override fun arenaSpawn(arenaKey: String): Tile? = null

    override fun arenaTiers(arenaKey: String): List<CombatTier> = emptyList()

    override fun autospawnIntervalTicks(arenaKey: String): Int = 0

    override fun autospawnTarget(arenaKey: String): Int = 0

    override fun arenaContains(arenaKey: String, tier: CombatTier): Boolean = false

    /** Pest control: PvE minigame, no PK loot — bot keeps its kit. */
    override fun shouldDropItems(player: Player, tier: CombatTier): Boolean = false

    /** Pest control respawn defers to engine default until the wave/role logic ships. */
    override fun respawnTile(tier: CombatTier): Tile? = null
}
