package content.bot.combat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Per-minigame lifecycle policy for combat bots. One context owns the spawn/death/retreat rules
 * for a family of arenas (clan wars, wilderness, pest control, etc.). The generic script
 * machinery in [content.bot.BotCommands] dispatches each event to the context that [handles]
 * the dying/retreating bot's tier.
 *
 * Adding a new context:
 *  1. Implement this interface with a new ID and an `activityId` prefix in [handles].
 *  2. Register it with [CombatBotContexts.register] from `BotCommands.init`.
 *  3. Add a corresponding `*.bots.toml` entry that creates activities under that prefix and a
 *     templates file (see `minigame_combat.templates.toml` for the PvP example).
 */
interface CombatBotContext {

    /** Stable identifier — used in logs and (eventually) settings paths. */
    val id: String

    /**
     * Areas whose `entered` event should be dispatched to [onAreaEntered]. The script in
     * BotCommands.kt registers one engine listener per name returned here at init time.
     */
    val subscribedAreas: Set<String>
        get() = emptySet()

    /** Does this context own the given tier? Typically `tier.activityId.startsWith(prefix)`. */
    fun handles(tier: CombatTier): Boolean

    /** Refresh internal state from cache tables. Called on world spawn and settings reload. */
    fun load()

    /** Arena keys the context exposes for `::combatbots <arena>` autocompletion + dispatch. */
    fun arenaKeys(): Set<String>

    /** Random spawn tile for an arena, or null if the arena key is unknown. */
    fun arenaSpawn(arenaKey: String): Tile?

    /** Tier pool for an arena. Empty if the key is unknown. */
    fun arenaTiers(arenaKey: String): List<CombatTier>

    /** Arena keys that should have a periodic top-up timer wired. */
    fun autospawnArenaKeys(): List<String> = emptyList()

    /** Ticks between top-up checks for a given arena. */
    fun autospawnIntervalTicks(arenaKey: String): Int

    /** Target bot count for the arena (typically a Settings lookup); 0 means disabled. */
    fun autospawnTarget(arenaKey: String): Int

    /** Does the given tier belong to the given arena's pool? Used for current-count math. */
    fun arenaContains(arenaKey: String, tier: CombatTier): Boolean

    /** Drop policy on death: true → bot drops its kit (PK loot piñata), false → kit kept. */
    fun shouldDropItems(player: Player, tier: CombatTier): Boolean

    /** Override the default home respawn tile for this tier. Null falls through to engine default. */
    fun respawnTile(tier: CombatTier): Tile?

    /**
     * Called when a bot owned by this context enters an area listed in [subscribedAreas].
     * This is a notification hook (logging, metrics) — the dispatcher inspects
     * [shouldRefreshOnAreaEntered] separately to decide whether to re-apply the bot's tier.
     */
    fun onAreaEntered(player: Player, tier: CombatTier, areaId: String) {}

    /**
     * Should the bot have its tier (kit, levels, special, brew counter) re-applied after
     * entering [areaId]? Used by the retreat-refresh path: a dangerous-arena bot teleporting
     * out via games necklace returns true so the dispatcher restocks before the bot walks
     * back through the portal. Defaults to false.
     */
    fun shouldRefreshOnAreaEntered(player: Player, tier: CombatTier, areaId: String): Boolean = false
}
