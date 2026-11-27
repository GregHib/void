package content.bot.allocation

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * [BotAllocator]s loaded from the `bot_allocations` table, along with any Kotlin [AllocationHooks] registered for them.
 */
object BotAllocations {
    private val logger = InlineLogger("BotAllocations")
    private var allocations: Map<String, BotAllocator> = emptyMap()
    private val hooks = mutableMapOf<String, AllocationHooks>()

    val all: Collection<BotAllocator>
        get() = allocations.values

    val ids: Set<String>
        get() = allocations.keys

    fun getOrNull(id: String): BotAllocator? = allocations[id]

    fun get(id: String): BotAllocator = allocations[id] ?: error("Unknown bot allocation '$id'.")

    /**
     * Register lifecycle [hooks] for allocation [id], must be called before allocations are loaded.
     */
    fun register(id: String, hooks: AllocationHooks) {
        this.hooks[id] = hooks
        allocations[id]?.hooks = hooks
    }

    /**
     * Areas any registered hooks want `entered` events for
     */
    val subscribedAreas: Set<String>
        get() = hooks.values.flatMapTo(mutableSetOf()) { it.subscribedAreas }

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
        )
        return when (val mode = row.stringOrNull("mode") ?: "capacity") {
            "capacity" -> CapacityAllocator(config)
            "weighted" -> {
                val roles = row.stringListOrNull("roles").orEmpty().map { ref ->
                    role(Rows.getOrNull(ref) ?: error("Unknown role '$ref' in bot allocation '$id'."), activityExists)
                }
                require(roles.isNotEmpty()) { "Proportional bot allocation '$id' has no roles." }
                WeightedAllocator(config, roles)
            }
            else -> error("Unknown mode '$mode' in bot allocation '$id', expected capacity or proportional.")
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
        for (allocator in allocators) {
            allocator.hooks = hooks[allocator.id] ?: AllocationHooks.Default
        }
        allocations = allocators.associateBy { it.id }
    }

    fun clear() {
        allocations = emptyMap()
        hooks.clear()
    }
}
