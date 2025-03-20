package world.gregs.voidps.engine.entity.item.drop

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.timedLoad

class DropTables {

    private lateinit var tables: Map<String, DropTable>

    fun get(key: String) = tables[key]

    fun getValue(key: String) = tables.getValue(key)

    fun load(path: String = Settings["spawns.drops"], itemDefinitions: ItemDefinitions? = null): DropTables {
        timedLoad("drop table") {
            val tables = Object2ObjectOpenHashMap<String, DropTable>(10, Hash.VERY_FAST_LOAD_FACTOR)
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
                                drops.add(readDrops(tables, itemDefinitions))
                            }
                            else -> throw IllegalArgumentException("Unexpected table key: '$key' ${exception()}")
                        }
                    }
                    tables[tableName] = DropTable(type, roll, drops, chance)
                }
            }
            this.tables = tables
            tables.size
        }
        return this
    }

    private fun ConfigReader.readDrops(tables: Map<String, DropTable>, itemDefinitions: ItemDefinitions?): Drop {
        var type = TableType.First
        var table = ""
        var members = false
        var chance: Int? = null
        var roll = 0
        var id = ""
        var min = 1
        var max = 1
        var owns: String? = null
        var lacks: String? = null
        val drops = ObjectArrayList<Drop>()
        while (nextEntry()) {
            when (val dropKey = key()) {
                "type" -> type = TableType.byName(string())
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
                "drops" -> while (nextElement()) {
                    drops.add(readDrops(tables, itemDefinitions))
                }
                else -> throw IllegalArgumentException("Unexpected drop key: '$dropKey' ${exception()}")
            }
        }
        if (drops.isNotEmpty()) {
            return DropTable(type, roll, drops, chance ?: -1)
        } else if (table != "") {
            val dropTable = tables[table]
            require(dropTable != null) { "Unable to find drop table with name '${table}'." }
            return dropTable
        } else if (id != "") {
            require(itemDefinitions == null || id == "nothing" || itemDefinitions.getOrNull(id) != null) { "Unable to find item with id '${id}'." }
            return ItemDrop(id = id, min = min, max = max, chance = chance ?: 1, members = members, owns = owns, lacks = lacks)
        } else {
            throw IllegalStateException("Unexpected drop entry. ${exception()}")
        }
    }
}