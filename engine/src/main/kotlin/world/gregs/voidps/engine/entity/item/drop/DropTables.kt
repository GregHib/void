package world.gregs.voidps.engine.entity.item.drop

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toPascalCase
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.wildcardEquals

class DropTables {

    lateinit var tables: Map<String, DropTable>
    private val logger = InlineLogger()

    fun get(key: String) = tables[key]

    fun getValue(key: String) = tables.getValue(key)

    fun load(paths: List<String>): DropTables {
        tables = parse(paths)
        return this
    }

    fun parse(paths: List<String>): Map<String, DropTable> {
        val tables = Object2ObjectOpenHashMap<String, DropTable>(10, Hash.VERY_FAST_LOAD_FACTOR)
        val gates = Object2ObjectOpenHashMap<String, List<Gate>>(10, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val tableName = section()
                    var roll = 1
                    var chance = 1
                    var type = TableType.First
                    val drops = ObjectArrayList<Drop>()
                    val dropGates = ObjectArrayList<Gate>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "roll" -> roll = int()
                            "type" -> type = TableType.byName(string())
                            "chance" -> chance = int()
                            "drops" -> while (nextElement()) {
                                drops.add(readItemDrop(roll, dropGates))
                            }
                            else -> throw IllegalArgumentException("Unexpected table key: '$key' ${exception()}")
                        }
                    }
                    if (tables.containsKey(tableName)) {
                        logger.warn { "Duplicate drop table '$tableName' in $path overrides an earlier definition." }
                    }
                    tables[tableName] = DropTable(type, roll, drops, chance)
                    gates[tableName] = dropGates
                }
            }
        }
        for ((tableName, table) in tables) {
            for (i in table.drops.indices) {
                val drop = table.drops[i]
                if (drop is ReferenceTable) {
                    val dropTable = tables[drop.tableName]
                    require(dropTable != null) { "Unable to find drop table with name '${drop.tableName}'." }
                    (table.drops as MutableList<Drop>)[i] = dropTable.copy(roll = drop.roll ?: dropTable.roll, chance = if (drop.chance == -1) dropTable.chance else drop.chance, predicate = dropTable.predicate ?: drop.predicate)
                }
            }
        }
        for ((tableName, table) in tables) {
            if (table.type == TableType.All || table.roll <= 1) {
                continue
            }
            val dropGates = gates[tableName] ?: continue
            for (world in booleanArrayOf(true, false)) {
                val unreachable = unreachable(table, dropGates, world)
                if (unreachable.isNotEmpty()) {
                    val type = if (world) "members" else "free-to-play"
                    logger.warn { "Drop table '$tableName' chances exceed roll ${table.roll} on $type worlds; these drops can never be selected: ${unreachable.joinToString()}." }
                }
            }
        }
        return tables
    }

    /**
     * Conditions on a drop that decide whether it can be rolled at all.
     * @param members world type the drop is limited to, null for both
     * @param key what the condition reads (a variable, skill or item), null when unconditional
     * @param condition the value [key] is tested against; drops with the same [key] but a different [condition] are mutually exclusive
     */
    internal data class Gate(val label: String, val members: Boolean? = null, val key: String? = null, val condition: String? = null)

    /**
     * Drops in [table] that can never be rolled on a members or free-to-play [world].
     * Conditional drops are assumed to pass (the worst case a player can hit), except drops gated on the same
     * key with different conditions (e.g. quest started/completed), which can't apply at once so only the largest counts.
     */
    internal fun unreachable(table: DropTable, gates: List<Gate>, world: Boolean): List<String> {
        val unreachable = mutableListOf<String>()
        for (i in table.drops.indices) {
            val gate = gates.getOrNull(i) ?: Gate("")
            if (table.drops[i].chance <= 0 || (gate.members ?: world) != world) {
                continue
            }
            var total = 0
            val groups = mutableMapOf<String, MutableMap<String, Int>>()
            for (j in 0 until i) {
                val other = gates.getOrNull(j) ?: Gate("")
                val chance = table.drops[j].chance
                if (chance <= 0 || (other.members ?: world) != world) {
                    continue
                }
                when {
                    other.key == null -> total += chance
                    other.key == gate.key && other.condition != gate.condition -> {}
                    else -> groups.getOrPut(other.key) { mutableMapOf() }.merge(other.condition ?: "", chance, Int::plus)
                }
            }
            total += groups.values.sumOf { it.values.max() }
            if (total >= table.roll) {
                unreachable.add(gate.label)
            }
        }
        return unreachable
    }

    private data class ReferenceTable(val tableName: String, val roll: Int?, override val chance: Int, override val predicate: ((Player) -> Boolean)?) : Drop {
        override fun print(indent: Int, multiplier: Double) = "${"  ".repeat(indent)}Ref($tableName)"
    }

    private fun ConfigReader.readItemDrop(tableRoll: Int, gates: MutableList<Gate>): Drop {
        var table = ""
        var chance: Int? = null
        var roll: Int? = null
        var id = ""
        var min = 1
        var max = 1
        var members: Boolean? = null
        var owns: String? = null
        var lacks: String? = null
        var variable: String? = null
        var skill: String? = null
        var eq: Any? = null
        var default: Any? = null
        var withinMin: Int? = null
        var withinMax: Int? = null
        var underground: Boolean? = null
        var negated = false
        while (nextEntry()) {
            when (val dropKey = key()) {
                "table" -> table = string()
                "chance" -> chance = int()
                "id" -> id = string()
                "amount", "charges" -> {
                    min = int()
                    max = min
                }
                "min" -> min = int()
                "max" -> max = int()
                "roll" -> roll = int()
                "lacks" -> lacks = string()
                "owns" -> owns = string()
                "members" -> members = boolean()
                "variable" -> variable = string()
                "skill" -> skill = string()
                "equals" -> eq = value()
                "not_equals" -> {
                    eq = value()
                    negated = true
                }
                "default" -> default = value()
                "within_min" -> withinMin = int()
                "within_max" -> withinMax = int()
                "underground" -> underground = boolean()
                else -> throw IllegalArgumentException("Unexpected drop key: '$dropKey' ${exception()}")
            }
        }
        if (roll != null && tableRoll != 1) {
            logger.warn { "Found roll override inside roll based table. Did you mean to use 'chance' instead? ${exception()}" }
        }
        val key = variable ?: skill?.let { "skill:$it" } ?: owns?.let { "owns:$it" } ?: lacks?.let { "lacks:$it" } ?: underground?.let { "underground" }
        val condition = if (key == null) null else "$negated:$eq:$withinMin:$withinMax:$underground"
        gates.add(Gate(table.ifEmpty { id }, members, key, condition))
        val predicate = dropPredicate(owns, lacks, variable, skill, negated, eq, default, withinMin, withinMax, members, underground)
        if (table != "") {
            return ReferenceTable(table, roll, chance ?: -1, predicate)
        }
        require(id == "nothing" || !ItemDefinitions.loaded || ItemDefinitions.getOrNull(id) != null) { "Unable to find item with id '$id'." }
        return ItemDrop(
            id = id,
            amount = min..max,
            chance = chance ?: 1,
            predicate = predicate,
        )
    }

    internal fun dropPredicate(
        owns: String? = null,
        lacks: String? = null,
        variable: String? = null,
        skill: String? = null,
        negated: Boolean = false,
        eq: Any? = null,
        default: Any? = null,
        withinMin: Int? = null,
        withinMax: Int? = null,
        members: Boolean? = null,
        underground: Boolean? = null,
    ): ((Player) -> Boolean)? {
        val predicates = mutableListOf<((Player) -> Boolean)>()
        if (owns != null || lacks != null) {
            predicates.add { (owns == null || ownsItem(it, owns)) && (lacks == null || !ownsItem(it, lacks)) }
        }
        if (members != null) {
            predicates.add { World.members == members }
        }
        if (underground != null) {
            predicates.add { (it.tile.y >= UNDERGROUND_Y) == underground }
        }
        if (skill != null) {
            val skill = Skill.of(skill.toPascalCase())!!
            if (negated) {
                if (eq is Int) {
                    predicates.add { it.levels.getMax(skill) != eq }
                }
            } else {
                if (eq is Int) {
                    predicates.add { it.levels.getMax(skill) == eq }
                } else if (withinMin != null && withinMax != null) {
                    val within = withinMin..withinMax
                    predicates.add { it.levels.getMax(skill) !in within }
                }
            }
        }
        if (variable != null) {
            if (negated) {
                if (eq != null) {
                    when (default) {
                        is Int -> predicates.add { it[variable, default] != eq }
                        is String -> predicates.add { it[variable, default] != eq }
                        is Double -> predicates.add { it[variable, default] != eq }
                        is Long -> predicates.add { it[variable, default] != eq }
                        is Boolean -> predicates.add { it[variable, default] != eq }
                        else -> when (eq) {
                            is Int -> predicates.add { it.get<Int>(variable) != eq }
                            is String -> predicates.add { it.get<String>(variable) != eq }
                            is Double -> predicates.add { it.get<Double>(variable) != eq }
                            is Long -> predicates.add { it.get<Long>(variable) != eq }
                            is Boolean -> predicates.add { it.get<Boolean>(variable) != eq }
                            else -> {}
                        }
                    }
                } else if (withinMin != null && withinMax != null) {
                    val within = withinMin..withinMax
                    predicates.add { it[variable, default ?: -1] !in within }
                }
            } else {
                if (eq != null) {
                    when (default) {
                        is Int -> predicates.add { it[variable, default] == eq }
                        is String -> predicates.add { it[variable, default] == eq }
                        is Double -> predicates.add { it[variable, default] == eq }
                        is Long -> predicates.add { it[variable, default] == eq }
                        is Boolean -> predicates.add { it[variable, default] == eq }
                        else -> when (eq) {
                            is Int -> predicates.add { it.get<Int>(variable) == eq }
                            is String -> predicates.add { it.get<String>(variable) == eq }
                            is Double -> predicates.add { it.get<Double>(variable) == eq }
                            is Long -> predicates.add { it.get<Long>(variable) == eq }
                            is Boolean -> predicates.add { it.get<Boolean>(variable) == eq }
                            else -> {}
                        }
                    }
                } else if (withinMin != null && withinMax != null) {
                    val within = withinMin..withinMax
                    predicates.add { it[variable, default ?: -1] in within }
                }
            }
        }
        return when (predicates.size) {
            0 -> null
            1 -> predicates[0]
            2 -> {
                { predicates[0](it) && predicates[1](it) }
            }
            else -> {
                { player -> predicates.all { it(player) } }
            }
        }
    }

    private val inventories = listOf("inventory", "worn_equipment", "bank")

    companion object {
        /** Map squares at or above this y are underground (dungeons and caves). */
        private const val UNDERGROUND_Y = 6400
    }

    private fun ownsItem(player: Player, item: String): Boolean {
        for (inventory in inventories) {
            val items = player.inventories.inventory(inventory).items
            if (items.any { wildcardEquals(item, it.id) }) {
                return true
            }
        }
        return false
    }
}
