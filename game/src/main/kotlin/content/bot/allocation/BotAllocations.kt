package content.bot.allocation

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * [BotAllocator]s loaded from the `bot_allocations` table.
 */
object BotAllocations {
    private val logger = InlineLogger("BotAllocations")
    private var allocations: Map<String, BotAllocator> = emptyMap()

    val all: Collection<BotAllocator>
        get() = allocations.values

    val ids: Set<String>
        get() = allocations.keys

    fun getOrNull(id: String): BotAllocator? = allocations[id]

    fun get(id: String): BotAllocator = allocations[id] ?: error("Unknown bot allocation '$id'.")

    /**
     * Load allocations from the `bot_allocations` table, [activityExists] validates role activities.
     */
    fun load(activityExists: (String) -> Boolean): BotAllocations {
        val table = Tables.getOrNull("bot_allocations")
        if (table == null) {
            logger.warn { "No bot_allocations table found." }
            set(emptyList())
            return this
        }
        set(table.rows().map { row -> parse(row, activityExists) })
        return this
    }

    internal fun parse(row: RowDefinition, activityExists: (String) -> Boolean): BotAllocator {
        val id = row.rowId
        val areas = row.stringListOrNull("spawn_areas") ?: emptyList()
        require(areas.isNotEmpty()) { "Bot allocation '$id' has no spawn areas." }
        for (area in areas) {
            require(Areas.getOrNull(area) != null) { "Unknown spawn area '$area' in bot allocation '$id'." }
        }
        val restock = row.stringListOrNull("restock_areas").orEmpty().toSet()
        for (area in restock) {
            require(Areas.getOrNull(area) != null) { "Unknown restock area '$area' in bot allocation '$id'." }
        }
        val config = AllocationConfig(
            id = id,
            count = row.intOrNull("count") ?: 0,
            countSetting = row.stringOrNull("count_setting"),
            spawnSeconds = row.intOrNull("spawn_seconds") ?: 60,
            spawnSecondsSetting = row.stringOrNull("spawn_seconds_setting"),
            spawnAreas = areas,
            refill = row.boolOrNull("refill") ?: false,
            saved = row.boolOrNull("saved") ?: false,
            starterItems = Tables.strIntListOrNull("${row.stringId}.starter_items") ?: emptyList(),
            restockAreas = restock,
        )
        return when (val mode = row.stringOrNull("mode") ?: "capacity") {
            "capacity" -> CapacityAllocator(config)
            "weighted" -> {
                val roles = row.stringListOrNull("roles").orEmpty().map { ref ->
                    role(Rows.getOrNull(ref) ?: error("Unknown role '$ref' in bot allocation '$id'."), activityExists)
                }
                require(roles.isNotEmpty()) { "Weighted bot allocation '$id' has no roles." }
                WeightedAllocator(config, roles)
            }
            else -> error("Unknown mode '$mode' in bot allocation '$id', expected capacity or weighted.")
        }
    }

    private fun role(row: RowDefinition, activityExists: (String) -> Boolean): BotRole {
        val activity = row.string("activity")
        require(activityExists(activity)) { "Unknown activity '$activity' in bot role '${row.rowId}'." }
        val builds = Tables.strIntListOrNull("${row.stringId}.builds").orEmpty().map { (build, weight) ->
            build(Rows.getOrNull(build) ?: error("Unknown build '$build' in bot role '${row.rowId}'.")) to weight
        }
        return BotRole(
            id = row.rowId,
            activity = activity,
            weight = row.intOrNull("weight") ?: 1,
            min = row.intOrNull("min") ?: 0,
            max = row.intOrNull("max"),
            builds = builds,
            combatStyle = row.stringOrNull("combat_style"),
        )
    }

    private fun build(row: RowDefinition): Build {
        val levels = LinkedHashMap<Skill, IntRange>()
        for (skill in Skill.all) {
            val range = row.intRangeOrNull(skill.name.lowercase()) ?: continue
            levels[skill] = range
        }
        return Build(row.rowId, levels)
    }

    fun set(allocators: List<BotAllocator>) {
        allocations = allocators.associateBy { it.id }
    }

    fun clear() {
        allocations = emptyMap()
    }
}
