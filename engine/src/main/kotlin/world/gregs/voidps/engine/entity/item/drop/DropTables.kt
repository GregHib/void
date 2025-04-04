package world.gregs.voidps.engine.entity.item.drop

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.ItemDefinitions
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
                        (table.drops as MutableList<Drop>)[i] = dropTable.copy(roll = drop.roll ?: dropTable.roll, chance = if (drop.chance == -1) dropTable.chance else drop.chance)
                    }
                }
            }
            this.tables = tables
            tables.size
        }
        return this
    }

    private data class ReferenceTable(val tableName: String, val roll: Int?, override val chance: Int) : Drop

    private fun ConfigReader.readItemDrop(itemDefinitions: ItemDefinitions?): Drop {
        var table = ""
        var members = false
        var chance: Int? = null
        var roll: Int? = null
        var id = ""
        var min = 1
        var max = 1
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
                "lacks" -> lacks = string()
                "roll" -> roll = int()
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
        if (table != "") {
            return ReferenceTable(table, roll, chance ?: -1)
        }
        require(itemDefinitions == null || id == "nothing" || itemDefinitions.getOrNull(id) != null) { "Unable to find item with id '${id}'." }
        val within = if (withinMin != null && withinMax != null) withinMin..withinMax else null
        return ItemDrop(
            id = id,
            min = min,
            max = max,
            chance = chance ?: 1,
            members = members,
            owns = owns,
            lacks = lacks,
            variable = variable,
            eq = eq,
            default = default,
            within = within,
            negated = negated,
        )
    }
}