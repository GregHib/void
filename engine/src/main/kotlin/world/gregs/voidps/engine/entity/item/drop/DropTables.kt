package world.gregs.voidps.engine.entity.item.drop

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.timedLoad

class DropTables {

    private lateinit var tables: Map<String, DropTable>

    fun get(key: String) = tables[key]

    fun getValue(key: String) = tables.getValue(key)

    fun load(paths: List<String>, itemDefinitions: ItemDefinitions? = null): DropTables {
        timedLoad("drop table") {
            val tables = Object2ObjectOpenHashMap<String, DropTable>(10, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val tableName = section()
                        var roll = 1
                        var chance = 1
                        var type = TableType.First
                        val drops = ObjectArrayList<Drop>()
                        while (nextPair()) {
                            when (val key = key()) {
                                "roll" -> roll = int()
                                "type" -> type = TableType.byName(string())
                                "chance" -> chance = int()
                                "drops" -> while (nextElement()) {
                                    drops.add(readItemDrop(itemDefinitions))
                                }
                                else -> throw IllegalArgumentException("Unexpected table key: '$key' ${exception()}")
                            }
                        }
                        tables[tableName] = DropTable(type, roll, drops, chance)
                    }
                }
            }
            for (table in tables.values) {
                for (i in table.drops.indices) {
                    val drop = table.drops[i]
                    if (drop is ReferenceTable) {
                        val dropTable = tables[drop.tableName]
                        require(dropTable != null) { "Unable to find drop table with name '${drop.tableName}'." }
                        (table.drops as MutableList<Drop>)[i] = dropTable.copy(roll = drop.roll ?: dropTable.roll, chance = if (drop.chance == -1) dropTable.chance else drop.chance, predicate = dropTable.predicate ?: drop.predicate)
                    }
                }
            }
            this.tables = tables
            tables.size
        }
        return this
    }

    private data class ReferenceTable(val tableName: String, val roll: Int?, override val chance: Int, override val predicate: ((Player) -> Boolean)?) : Drop

    private fun ConfigReader.readItemDrop(itemDefinitions: ItemDefinitions?): Drop {
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
        var eq: Any? = null
        var default: Any? = null
        var withinMin: Int? = null
        var withinMax: Int? = null
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
                "equals" -> eq = value()
                "not_equals" -> {
                    eq = value()
                    negated = true
                }
                "default" -> default = value()
                "within_min" -> withinMin = int()
                "within_max" -> withinMax = int()
                else -> throw IllegalArgumentException("Unexpected drop key: '$dropKey' ${exception()}")
            }
        }
        val predicate = dropPredicate(owns, lacks, variable, negated, eq, default, withinMin, withinMax, members)
        if (table != "") {
            return ReferenceTable(table, roll, chance ?: -1, predicate)
        }
        require(itemDefinitions == null || id == "nothing" || itemDefinitions.getOrNull(id) != null) { "Unable to find item with id '${id}'." }
        return ItemDrop(
            id = id,
            amount = min..max,
            chance = chance ?: 1,
            predicate = predicate
        )
    }

    internal fun dropPredicate(
        owns: String? = null,
        lacks: String? = null,
        variable: String? = null,
        negated: Boolean = false,
        eq: Any? = null,
        default: Any? = null,
        withinMin: Int? = null,
        withinMax: Int? = null,
        members: Boolean? = null
    ): ((Player) -> Boolean)? {
        val predicates = mutableListOf<((Player) -> Boolean)>()
        if (owns != null || lacks != null) {
            predicates.add { (owns == null || ownsItem(it, owns)) && (lacks == null || !ownsItem(it, lacks)) }
        }
        if (members != null) {
            predicates.add { World.members == members }
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
            1 -> predicates[0]
            2 -> {
                { predicates[0](it) && predicates[1](it) }
            }
            3 -> {
                { predicates[0](it) && predicates[1](it) && predicates[2](it) }
            }
            else -> null
        }
    }

    private val inventories = listOf("inventory", "worn_equipment", "bank")

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