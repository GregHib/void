package content.bot.combat

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

/**
 * Combat-bot context for Clan Wars FFA arenas. Owns the `clan_wars_*` activity-id family,
 * loads arena/tier data from the `clan_wars_arenas` and `clan_wars_tiers` cache tables, and
 * defines the dangerous-arena drop rule + the `clan_wars_teleport` retreat refresh policy.
 *
 * Spawn / death wiring lives in `BotCommands`; this class only carries the policy that used
 * to be inlined there.
 */
class ClanWarsBotContext : CombatBotContext {

    override val id: String = "clan_wars"

    override val subscribedAreas: Set<String> = setOf("clan_wars_teleport")

    private val logger = InlineLogger("ClanWarsBots")
    private val arenas = mutableMapOf<String, ClanWarsArena>()

    override fun handles(tier: CombatTier): Boolean = tier.activityId.startsWith("clan_wars_")

    override fun load() {
        arenas.clear()
        val arenaTable = Tables.getOrNull("clan_wars_arenas") ?: return
        val tierTable = Tables.getOrNull("clan_wars_tiers") ?: return
        val tiersById = tierTable.rows().associate { row -> row.rowId to row.toCombatTier() }
        for (row in arenaTable.rows()) {
            val spawnArea = row.string("spawn_area")
            val tiers = row.stringList("tiers").mapNotNull { tiersById[it] }
            if (tiers.isEmpty()) {
                logger.warn { "No tiers resolved for arena '${row.rowId}'." }
                continue
            }
            arenas[row.rowId] = ClanWarsArena(spawnArea, tiers)
        }
    }

    override fun arenaKeys(): Set<String> = arenas.keys

    override fun arenaSpawn(arenaKey: String): Tile? {
        val arena = arenas[arenaKey] ?: return null
        return Areas[arena.spawnArea].random()
    }

    override fun arenaTiers(arenaKey: String): List<CombatTier> = arenas[arenaKey]?.tiers ?: emptyList()

    override fun autospawnArenaKeys(): List<String> = AUTOSPAWN_ARENAS

    override fun autospawnIntervalTicks(arenaKey: String): Int = TimeUnit.SECONDS.toTicks(Settings["bots.combat.$arenaKey.spawnSeconds", 2])

    override fun autospawnTarget(arenaKey: String): Int = Settings["bots.combat.$arenaKey.count", 0]

    override fun arenaContains(arenaKey: String, tier: CombatTier): Boolean = tier.activityId.startsWith("${arenaKey}_")

    override fun shouldDropItems(player: Player, tier: CombatTier): Boolean {
        if (tier.activityId.startsWith("clan_wars_ffa_dangerous_")) return true
        return player.tile in Areas["clan_wars_ffa_dangerous_arena"]
    }

    override fun respawnTile(tier: CombatTier): Tile? {
        val arena = arenas.values.firstOrNull { a -> a.tiers.any { t -> t.activityId == tier.activityId } } ?: return null
        return Areas[arena.spawnArea].random()
    }

    /**
     * Logs the retreat — actual restocking (applyTier + state reset) runs in the generic
     * dispatcher in BotCommands when [shouldRefreshOnAreaEntered] returns true.
     */
    override fun onAreaEntered(player: Player, tier: CombatTier, areaId: String) {
        if (areaId != "clan_wars_teleport") return
        if (!tier.activityId.startsWith("clan_wars_ffa_dangerous_")) return
        logger.info { "PvP bot retreat: '${player.accountName}' tier=${tier.activityId} teleported to clan_wars_teleport" }
    }

    /**
     * Dangerous-arena retreat: bot teleported out via jewellery → restock before it walks
     * back through the portal so it doesn't re-enter the arena empty-inventory.
     */
    override fun shouldRefreshOnAreaEntered(player: Player, tier: CombatTier, areaId: String): Boolean = areaId == "clan_wars_teleport" && tier.activityId.startsWith("clan_wars_ffa_dangerous_")

    private fun RowDefinition.toCombatTier(): CombatTier {
        val skillNames = stringList("skills")
        val values = intList("levels")
        require(skillNames.size == values.size) { "clan_wars_tiers.$rowId: skills/levels size mismatch." }
        val levels = LinkedHashMap<Skill, Int>(skillNames.size)
        for ((index, name) in skillNames.withIndex()) {
            val skillId = Skill.map[name] ?: error("clan_wars_tiers.$rowId: unknown skill '$name'.")
            levels[Skill.all[skillId]] = values[index]
        }
        return CombatTier(activityId = rowId, levels = levels, style = string("combat_style"))
    }

    private data class ClanWarsArena(val spawnArea: String, val tiers: List<CombatTier>)

    companion object {
        private val AUTOSPAWN_ARENAS = listOf("clan_wars_ffa_safe", "clan_wars_ffa_dangerous")
    }
}
