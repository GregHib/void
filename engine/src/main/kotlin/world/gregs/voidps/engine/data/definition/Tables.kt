package world.gregs.voidps.engine.data.definition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.decodeValue
import world.gregs.voidps.engine.data.snapshot.encodeValue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.collections.set

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

    fun get(table: String) = definitions[table] ?: error("Table '$table' not found")

    fun getOrNull(table: String) = definitions[table]

    /*
        Primitives
     */

    fun bool(path: String): Boolean = get(path, ColumnType.ColumnBoolean)

    fun boolOrNull(path: String): Boolean? = getOrNull(path, ColumnType.ColumnBoolean)

    fun int(path: String): Int = get(path, ColumnType.ColumnInt)

    fun intOrNull(path: String): Int? = getOrNull(path, ColumnType.ColumnInt)

    fun string(path: String): String = get(path, ColumnType.ColumnString)

    fun stringOrNull(path: String): String? = getOrNull(path, ColumnType.ColumnString)

    fun intRange(path: String): IntRange = get(path, ColumnType.ColumnIntRange)

    fun intRangeOrNull(path: String): IntRange? = getOrNull(path, ColumnType.ColumnIntRange)

    /*
        Entities
     */

    fun item(path: String): String = ItemDefinitions.get(get(path, ColumnType.ColumnEntity)).stringId

    fun itemOrNull(path: String): String? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        val item = ItemDefinitions.getOrNull(id) ?: return null
        return item.stringId
    }

    fun tile(path: String): Tile = Tile(get(path, ColumnType.ColumnInt))

    fun tileOrNull(path: String): Tile? {
        val id = getOrNull(path, ColumnType.ColumnInt) ?: return null
        return Tile(id)
    }

    fun obj(path: String): String = ObjectDefinitions.get(get(path, ColumnType.ColumnEntity)).stringId

    fun objOrNull(path: String): String? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        val obj = ObjectDefinitions.getOrNull(id) ?: return null
        return obj.stringId
    }

    fun npc(path: String): String = NPCDefinitions.get(get(path, ColumnType.ColumnEntity)).stringId

    fun npcOrNull(path: String): String? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        val npc = NPCDefinitions.getOrNull(id) ?: return null
        return npc.stringId
    }

    fun anim(path: String): String = AnimationDefinitions.get(get(path, ColumnType.ColumnEntity)).stringId

    fun animOrNull(path: String): String? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        val anim = AnimationDefinitions.getOrNull(id) ?: return null
        return anim.stringId
    }

    fun gfx(path: String): String = GraphicDefinitions.get(get(path, ColumnType.ColumnEntity)).stringId

    fun gfxOrNull(path: String): String? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        val gfx = GraphicDefinitions.getOrNull(id) ?: return null
        return gfx.stringId
    }

    fun skill(path: String): Skill = Skill.all[get(path, ColumnType.ColumnEntity)]

    fun skillOrNull(path: String): Skill? {
        val id = getOrNull(path, ColumnType.ColumnEntity) ?: return null
        return Skill.all[id]
    }

    fun row(path: String): RowDefinition = Rows.get(get(path, ColumnType.ColumnString))

    fun rowOrNull(path: String): RowDefinition? {
        val id = getOrNull(path, ColumnType.ColumnString) ?: return null
        return Rows.getOrNull(id)
    }

    fun rowList(path: String): List<RowDefinition> = get(path, ColumnType.StringList).map { Rows.get(it) }

    fun rowListOrNull(path: String): List<RowDefinition>? {
        val ids = getOrNull(path, ColumnType.StringList) ?: return null
        return ids.mapNotNull { Rows.getOrNull(it) }
    }

    /*
        Primitive Lists
     */

    fun intList(path: String): List<Int> = get(path, ColumnType.IntList)

    fun intListOrNull(path: String): List<Int>? = getOrNull(path, ColumnType.IntList)

    fun stringList(path: String): List<String> = get(path, ColumnType.StringList)

    fun stringListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.StringList)

    fun boolList(path: String): List<Boolean> = get(path, ColumnType.BooleanList)

    fun boolListOrNull(path: String): List<Boolean>? = getOrNull(path, ColumnType.BooleanList)

    /*
        Entity Lists
     */

    fun itemList(path: String): List<String> = get(path, ColumnType.IntList).map { ItemDefinitions.get(it).stringId }

    fun itemListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.IntList)?.map { ItemDefinitions.get(it).stringId }

    fun tileList(path: String): List<Tile> = get(path, ColumnType.IntList).map { Tile(it) }

    fun tileListOrNull(path: String): List<Tile>? = getOrNull(path, ColumnType.IntList)?.map { Tile(it) }

    fun objList(path: String): List<String> = get(path, ColumnType.IntList).map { ObjectDefinitions.get(it).stringId }

    fun objListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.IntList)?.map { ObjectDefinitions.get(it).stringId }

    fun npcList(path: String): List<String> = get(path, ColumnType.IntList).map { NPCDefinitions.get(it).stringId }

    fun npcListOrNull(path: String): List<String>? = getOrNull(path, ColumnType.IntList)?.map { NPCDefinitions.get(it).stringId }

    /*
        Primitive Pairs
     */

    fun itemPair(path: String): Pair<String, String> {
        val pair = get(path, ColumnType.IntIntPair)
        val first = ItemDefinitions.get(pair.first).stringId
        val second = ItemDefinitions.get(pair.second).stringId
        return Pair(first, second)
    }

    fun skillPair(path: String): Pair<Skill, Int> {
        val (id, level) = get(path, ColumnType.IntIntPair)
        return Pair(Skill.all[id], level)
    }

    fun skillPairOrNull(path: String): Pair<Skill, Int>? {
        val (id, level) = getOrNull(path, ColumnType.IntIntPair) ?: return null
        return Pair(Skill.all[id], level)
    }

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

    private fun <T : Any> get(table: String, column: String, row: Int, type: ColumnType<T>): T = definitions[table]?.get(column, row, type) ?: error("Table '$table' not found")

    private fun <T : Any> getOrNull(table: String, column: String, row: Int, type: ColumnType<T>): T? {
        val definition = definitions[table] ?: error("Table '$table' not found")
        return definition.getOrNull(column, row, type)
    }

    fun <T : Any> get(path: String, type: ColumnType<T>): T {
        val (table, row, column) = path.split(".")
        val id = Rows.ids["$table.$row"] ?: error("Row '$row' not found for $path")
        return get(table, column, id, type)
    }

    private fun <T : Any> getOrNull(path: String, type: ColumnType<T>): T? {
        val (table, row, column) = path.split(".")
        val id = Rows.ids["$table.$row"] ?: return null
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

    fun install(state: TablesState) {
        definitions = state.definitions
        loaded = true
        Rows.set(state.rows, state.ids)
    }

    /**
     * Parses [paths] and installs them straight away.
     */
    fun load(paths: List<String>): Tables {
        install(parse(paths))
        return this
    }

    internal fun parse(paths: List<String>): TablesState {
        val definitions = mutableMapOf<String, TableBuilder>()
        val rows = mutableListOf<RowDefinition>()
        val ids = mutableMapOf<String, Int>()
        for (path in paths) {
            Config.fileReader(path, 512) {
                try {
                    while (nextSection()) {
                        val stringId = section()
                        if (stringId.contains(".")) {
                            val table = stringId.substringBefore(".")
                            readTableRow(this, definitions, rows, ids, table, stringId)
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
        return TablesState(definitions.mapValues { it.value.build() }.toMap(), rows.toTypedArray(), ids)
    }

    private fun readTableRow(reader: ConfigReader, definitions: MutableMap<String, TableBuilder>, rows: MutableList<RowDefinition>, ids: MutableMap<String, Int>, key: String, rowName: String) {
        val builder = definitions[key]
        requireNotNull(builder) { "Table header not found '$key' at ${reader.exception()}." }
        val idReader = builder.idReader
        if (idReader != null) {
            val rowId = rowName.substringAfter(".")
            requireNotNull(idReader.ids[rowId]) { "Unable to find entity '$rowId' for row id" }
        }
        val row = arrayOfNulls<Any>(builder.readers.size)
        while (reader.nextPair()) {
            val name = reader.key()
            val index = builder.columns[name]
            requireNotNull(index) { "Column '$name' not found in table '$key' at ${reader.exception()}." }
            val column = builder.readers[index]
            if (column is ColumnReader.ReaderClone) {
                val rowId = column.read(reader)
                val clone = ids["$key.$rowId"]?.let { rows[it].data }
                requireNotNull(clone) { "Row '$rowId' to clone not found in table '$key' at ${reader.exception()}." }
                for (i in row.indices) {
                    if (row[i] == null) {
                        row[i] = clone[i]
                    }
                }
            } else {
                row[index] = column.read(reader)
            }
        }
        require(!ids.containsKey(rowName)) { "Duplicate row id found '$rowName' at ${reader.exception()}." }
        val id = rows.size
        ids[rowName] = id
        rows.add(RowDefinition(id, row, rowName))
        builder.addRow(id)
    }

    private fun readTableHeader(reader: ConfigReader, definitions: MutableMap<String, TableBuilder>, stringId: String) {
        require(!definitions.containsKey(stringId)) { "Duplicate table id found '$stringId' at ${reader.exception()}." }
        val builder = TableBuilder()
        definitions[stringId] = builder
        while (reader.nextPair()) {
            val key = reader.key()
            if (key == "row_id") {
                val reader = ColumnReader.reader(reader.string())
                if (reader is ColumnReader.ReaderEntity) {
                    builder.idReader = reader
                }
            } else if (key.endsWith("_default")) {
                val name = key.removeSuffix("_default")
                val index = builder.columns[name]
                requireNotNull(index) { "Column '$name' not found for default at ${reader.exception()}." }
                val read = builder.readers[index]
                val value = read.read(reader)
                builder.setDefault(index, name, value)
            } else {
                val type = reader.string()
                builder.addColumn(key, type)
            }
        }
    }

    private class TableBuilder {
        val columns = mutableMapOf<String, Int>()
        val readers = mutableListOf<ColumnReader<*>>()
        val defaults = mutableListOf<Any?>()
        val rows = mutableListOf<Int>()
        private var columnIndex = 0
        var idReader: ColumnReader.ReaderEntity? = null

        fun setDefault(index: Int, name: String, value: Any) {
            require(index < columnIndex) { "Default column index out of bounds '$name' - has type been set?" }
            defaults[index] = value
        }

        fun addColumn(name: String, type: String) {
            require(!columns.containsKey(name)) { "Duplicate column name $name in table definition" }
            columns[name] = columnIndex++
            val reader = ColumnReader.reader(type)
            readers.add(reader)
            defaults.add(reader.type.default)
        }

        fun addRow(id: Int) = rows.add(id)

        fun build() = TableDefinition(
            columns,
            readers.map { it.type }.toTypedArray(),
            defaults.toTypedArray(),
            rows.toIntArray(),
        )
    }
}

class TablesState(
    val definitions: Map<String, TableDefinition>,
    val rows: Array<RowDefinition>,
    val ids: Map<String, Int>,
)

object TableLoader : Loader<TablesState> {
    override val name = "tables"
    override val files = listOf("definitions.tables")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader, ObjectLoader, NPCLoader, VariableLoader, AnimationLoader, GraphicLoader)
    override val codec = SnapshotFormat.codec(TablesStateSerializer)

    override fun load(context: LoadContext): TablesState = Tables.parse(context.paths(this))

    override fun install(state: TablesState) = Tables.install(state)
}

private object TablesStateSerializer : KSerializer<TablesState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.TablesState") {
        element("table", String.serializer().descriptor)
        element("column", String.serializer().descriptor)
        element("type", Byte.serializer().descriptor)
        element("cell", Byte.serializer().descriptor)
        element("rows", IntArraySerializer().descriptor)
        element("row", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: TablesState) {
        encoder.encodeInt(value.definitions.size)
        for ((name, table) in value.definitions) {
            encoder.encodeString(name)
            encoder.encodeInt(table.columns.size)
            for ((column, index) in table.columns) {
                encoder.encodeString(column)
                encoder.encodeInt(index)
            }
            encoder.encodeInt(table.types.size)
            for (type in table.types) {
                type(encoder, type)
            }
            encoder.encodeInt(table.default.size)
            for (cell in table.default) {
                encoder.encodeValue(cell)
            }
            encoder.encodeInt(table.rows.size)
            for (row in table.rows) {
                encoder.encodeInt(row)
            }
        }
        encoder.encodeInt(value.rows.size)
        for (row in value.rows) {
            encoder.encodeString(row.stringId)
            encoder.encodeInt(row.data.size)
            for (cell in row.data) {
                encoder.encodeValue(cell)
            }
        }
    }

    override fun deserialize(decoder: Decoder): TablesState {
        val tables = LinkedHashMap<String, TableDefinition>()
        repeat(decoder.decodeInt()) {
            val name = decoder.decodeString()
            val columns = LinkedHashMap<String, Int>()
            repeat(decoder.decodeInt()) {
                val column = decoder.decodeString()
                columns[column] = decoder.decodeInt()
            }
            val types = Array(decoder.decodeInt()) { type(decoder) }
            val defaults = Array(decoder.decodeInt()) { decoder.decodeValue() }
            tables[name] = TableDefinition(columns, types, defaults, IntArray(decoder.decodeInt()) { decoder.decodeInt() })
        }
        val ids = LinkedHashMap<String, Int>()
        val rows = Array(decoder.decodeInt()) { id ->
            val stringId = decoder.decodeString()
            ids[stringId] = id
            RowDefinition(id, Array(decoder.decodeInt()) { decoder.decodeValue() }, stringId)
        }
        return TablesState(tables, rows, ids)
    }

    private fun type(encoder: Encoder, type: ColumnType<*>) {
        when (type) {
            ColumnType.ColumnBoolean -> encoder.encodeByte(0)
            ColumnType.ColumnInt -> encoder.encodeByte(1)
            ColumnType.ColumnIntRange -> encoder.encodeByte(2)
            ColumnType.ColumnString -> encoder.encodeByte(3)
            ColumnType.ColumnEntity -> encoder.encodeByte(4)
            is ColumnType.ColumnPair<*, *> -> {
                encoder.encodeByte(5)
                type(encoder, type.one)
                type(encoder, type.two)
            }
            is ColumnType.ColumnTriple<*, *, *> -> {
                encoder.encodeByte(6)
                type(encoder, type.one)
                type(encoder, type.two)
                type(encoder, type.three)
            }
            is ColumnType.ColumnList<*> -> {
                encoder.encodeByte(7)
                type(encoder, type.type)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun type(decoder: Decoder): ColumnType<*> = when (val tag = decoder.decodeByte().toInt()) {
        0 -> ColumnType.ColumnBoolean
        1 -> ColumnType.ColumnInt
        2 -> ColumnType.ColumnIntRange
        3 -> ColumnType.ColumnString
        4 -> ColumnType.ColumnEntity
        5 -> ColumnType.ColumnPair(type(decoder) as ColumnType<Any>, type(decoder) as ColumnType<Any>)
        6 -> ColumnType.ColumnTriple(type(decoder) as ColumnType<Any>, type(decoder) as ColumnType<Any>, type(decoder) as ColumnType<Any>)
        7 -> ColumnType.ColumnList(type(decoder) as ColumnType<Any>)
        else -> throw IllegalStateException("Unknown column type tag $tag")
    }
}
