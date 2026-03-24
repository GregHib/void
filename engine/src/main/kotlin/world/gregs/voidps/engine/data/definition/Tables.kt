package world.gregs.voidps.engine.data.definition

import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.definition.ColumnType.ObjectType
import world.gregs.voidps.engine.data.definition.ColumnType.RowList
import world.gregs.voidps.engine.timedLoad

object Tables {

    var definitions: Map<String, TableDefinition> = emptyMap()
        private set

    var loaded = false
        private set

    val size: Int
        get() = definitions.size

    fun contains(table: String, column: String, rowIndex: Int): Boolean {
        val definition = definitions[table] ?: return false
        val index = definition.columns[column] ?: return false
        val rowId = definition.rows.getOrNull(rowIndex) ?: return false
        val row = Rows.getOrNull(rowId) ?: return false
        return row.data[index] != null
    }

    /*
        Primitives
     */

    fun bool(path: String): Boolean = get(path, ColumnType.BooleanType)

    fun boolOrNull(path: String): Boolean? = getOrNull(path, ColumnType.BooleanType)

    fun int(path: String): Int = get(path, ColumnType.IntType)

    fun intOrNull(path: String): Int? = getOrNull(path, ColumnType.IntType)

    fun string(path: String): String = get(path, ColumnType.StringType)

    fun stringOrNull(path: String): String? = getOrNull(path, ColumnType.StringType)

    /*
        Entities
     */

    fun item(path: String): String = get(path, ColumnType.ItemType)

    fun itemOrNull(path: String): String? = getOrNull(path, ColumnType.ItemType)

    fun obj(path: String): String = get(path, ObjectType)

    fun objOrNull(path: String): String? = getOrNull(path, ObjectType)

    fun npc(path: String): String = get(path, ColumnType.NPCType)

    fun npcOrNull(path: String): String? = getOrNull(path, ColumnType.NPCType)

    fun row(path: String): Array<Any?> = Rows.get(get(path, ColumnType.RowType)).data

    fun rowOrNull(path: String): Array<Any?>? {
        val id = getOrNull(path, ColumnType.RowType) ?: return null
        return Rows.getOrNull(id)?.data
    }

    /*
        Primitive Lists
     */

    fun intList(path: String): List<Int> = get(path, ColumnType.IntList)

    fun intListOrNull(path: String): List<Int>? = getOrNull(path, ColumnType.IntList)

    fun stringList(path: String): List<String> = get(path, ColumnType.StringList)

    fun stringListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.StringList)


    /*
        Entity Lists
     */

    fun itemList(path: String): List<String> = get(path, ColumnType.ItemList)

    fun itemListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.ItemList)

    fun objList(path: String): List<String> = get(path, ColumnType.ObjectList)

    fun objListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.ObjectList)

    fun npcList(path: String): List<String> = get(path, ColumnType.NPCList)

    fun npcListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.NPCList)


    /*
        Primitive Pairs
     */

    fun intPair(path: String): Pair<Int, Int> = get(path, ColumnType.IntIntPair)

    fun intPairOrNull(path: String): Pair<Int, Int>? = getOrNull(path, ColumnType.IntIntPair)

    fun strIntPair(path: String): Pair<String, Int> = get(path, ColumnType.StringIntPair)

    fun strIntPairOrNull(path: String): Pair<String, Int>? = getOrNull(path, ColumnType.StringIntPair)

    fun intStrPair(path: String): Pair<Int, String> = get(path, ColumnType.IntStringPair)

    fun intStrPairOrNull(path: String): Pair<Int, String>? = getOrNull(path, ColumnType.IntStringPair)

    fun intPairList(path: String): List<Pair<Int, Int>> = get(path, ColumnType.IntIntList)

    fun intPairListOrNull(path: String): List<Pair<Int, Int>>? = getOrNull(path, ColumnType.IntIntList)

    fun strIntList(path: String): List<Pair<String, Int>> = get(path, ColumnType.StringIntList)

    fun strIntListOrNull(path: String): List<Pair<String, Int>>? = getOrNull(path, ColumnType.StringIntList)

    fun intStrList(path: String): List<Pair<Int, String>> = get(path, ColumnType.IntStringList)

    fun intStrListOrNull(path: String): List<Pair<Int, String>>? = getOrNull(path, ColumnType.IntStringList)

    private fun <T : Any> get(table: String, column: String, row: Int, type: ColumnType<T, *>): T {
        return definitions[table]?.get(column, row, type) ?: error("Table '$table' not found")
    }

    private fun <T : Any> getOrNull(table: String, column: String, row: Int, type: ColumnType<T, *>): T? {
        val definition = definitions[table] ?: error("Table '$table' not found")
        return definition.getOrNull(column, row, type)
    }

    fun <T : Any> get(path: String, type: ColumnType<T, *>): T {
        val (table, row, column) = path.split(".")
        val id = Rows.ids[row] ?: error("Row '$row' not found")
        return get(table, column, id, type)
    }

    private fun <T : Any> getOrNull(path: String, type: ColumnType<T, *>): T? {
        val (table, row, column) = path.split(".")
        val id = Rows.ids[row] ?: error("Row '$row' not found")
        return getOrNull(table, column, id, type)
    }

    @TestOnly
    fun set(definitions: Map<String, TableDefinition>) {
        this.definitions = definitions
        loaded = true
    }

    fun clear() {
        this.definitions = emptyMap()
        loaded = false
    }

    fun load(paths: List<String>): Tables {
        require(ItemDefinitions.loaded) { "Item definitions must be loaded before tables" }
        require(ObjectDefinitions.loaded) { "Object definitions must be loaded before tables" }
        require(NPCDefinitions.loaded) { "NPC definitions must be loaded before tables" }
        timedLoad("table config") {
            val definitions = mutableMapOf<String, TableBuilder>()
            val rows = mutableListOf<RowDefinition>()
            val ids = mutableMapOf<String, Int>()
            for (path in paths) {
                Config.fileReader(path, 256) {
                    try {
                        while (nextSection()) {
                            val stringId = section()
                            if (stringId.contains(".")) {
                                val (key, rowName) = stringId.split(".")
                                readTableRow(this, definitions, rows, ids, key, rowName)
                            } else {
                                readTableHeader(this, definitions, stringId)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error("Error reading table definition at ${exception()}")
                    }
                }
            }
            set(definitions.mapValues { it.value.build() }.toMap())
            Rows.set(rows.toTypedArray(), ids)
            size
        }
        return this
    }

    private fun readTableRow(reader: ConfigReader, definitions: MutableMap<String, TableBuilder>, rows: MutableList<RowDefinition>, ids: MutableMap<String, Int>, key: String, rowName: String) {
        val builder = definitions[key]
        requireNotNull(builder) { "Table header not found '$key' at ${reader.exception()}." }
        val row = arrayOfNulls<Any>(builder.types.sumOf { it.size })
        while (reader.nextPair()) {
            val column = reader.key()
            val index = builder.columns[column]
            requireNotNull(index) { "Column '$column' not found in table '$key' at ${reader.exception()}." }
            val type = builder.types[index]
            type.set(row, index, reader)
        }
        require(!ids.containsKey(rowName)) { "Duplicate row id found '$rowName' at ${reader.exception()}." }
        val id = rows.size
        ids[rowName] = id
        rows.add(RowDefinition(row, rowName))
        builder.addRow(id)
    }

    private fun readTableHeader(reader: ConfigReader, definitions: MutableMap<String, TableBuilder>, stringId: String) {
        require(!definitions.containsKey(stringId)) { "Duplicate table id found '$stringId' at ${reader.exception()}." }
        val builder = TableBuilder()
        definitions[stringId] = builder
        while (reader.nextPair()) {
            val key = reader.key()
            if (key.endsWith("_default")) {
                val default = reader.value()
                builder.setDefault(key.removeSuffix("_default"), default)
            } else {
                val type = reader.string()
                builder.addColumn(key, type)
            }
        }
    }

    private class TableBuilder {
        val columns = mutableMapOf<String, Int>()
        val types = mutableListOf<ColumnType<*, *>>()
        val defaults = mutableListOf<Any?>()
        val rows = mutableListOf<Int>()
        private var columnIndex = 0

        fun setDefault(name: String, value: Any) {
            val index = columns[name]
            requireNotNull(index) { "Default column not found '$name'" }
            require(index < columnIndex) { "Default column index out of bounds '$name' - has type been set?" }
            defaults[index] = value
        }

        fun addColumn(name: String, type: String) {
            require(!columns.containsKey(name)) { "Duplicate column name $name in table definition" }
            columns[name] = columnIndex
            val columnType = ColumnType.type(type)
            columnIndex += columnType.size
            println("Add column $name ${columnIndex} ${columnType.size}")
            for (i in 0 until columnType.size) {
                types.add(columnType)
                defaults.add(columnType.default(i))
            }
        }

        fun addRow(id: Int) = rows.add(id)

        fun build() = TableDefinition(
            columns,
            types.toTypedArray(),
            defaults.toTypedArray(),
            rows.toIntArray(),
        )
    }
}