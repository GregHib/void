package world.gregs.voidps.engine.data.definition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.snapshot.decodeValue
import world.gregs.voidps.engine.data.snapshot.encodeValue
import kotlin.collections.set

internal object TablesStateSerializer : KSerializer<TablesState> {
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
