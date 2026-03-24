package world.gregs.voidps.engine.data.config

import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.ColumnType
import world.gregs.voidps.engine.data.definition.Rows

/**
 * DbTable
 */
data class TableDefinition(
    val columns: Map<String, Int>,
    val types: Array<ColumnType<*>>,
    val default: Array<Any?>,
    val rows: IntArray,
) {

    fun <T : Any> get(column: String, row: Int, type: ColumnType<T>): T = getOrNull(column, row, type) ?: type.default

    fun <T : Any> get(column: Int, row: Int, type: ColumnType<T>): T = getOrNull(column, row, type) ?: type.default

    fun <T : Any> getOrNull(column: String, row: Int, type: ColumnType<T>): T? {
        val columnIndex = columns[column] ?: return null
        return getOrNull(columnIndex, row, type)
    }

    fun <T : Any> getOrNull(column: Int, row: Int, type: ColumnType<T>): T? {
        return value(row, column, type)
    }

    private fun <T : Any> value(row: Int, column: Int, type: ColumnType<T>): T? {
        val id = rows.getOrNull(row) ?: return type.default
        val rows = Rows.getOrNull(id)?.data ?: return null
        val value = rows[column]
        return type.cast(value)
    }

    fun <T : Any> findOrNull(searchColumn: String, value: Any, column: String, type: ColumnType<T>): T? {
        val searchIndex = columns[searchColumn] ?: return null
        return findOrNull(searchIndex, value, column, type)
    }

    fun <T : Any> findOrNull(searchColumn: Int, value: Any, column: String, type: ColumnType<T>): T? {
        val columnIndex = columns[column] ?: return null
        val row = findOrNull(searchColumn, value) ?: return null
        return value(row, columnIndex, type)
    }

    fun findOrNull(column: Int, value: Any): Int? {
        for ((index, row) in rows.withIndex()) {
            val row = Rows.getOrNull(row)
            if (row != null && row.data[column] == value) {
                return index
            }
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TableDefinition

        if (columns != other.columns) return false
        if (!types.contentEquals(other.types)) return false
        if (!default.contentEquals(other.default)) return false
        if (!rows.contentEquals(other.rows)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columns.hashCode()
        result = 31 * result + types.contentHashCode()
        result = 31 * result + default.contentHashCode()
        result = 31 * result + rows.contentHashCode()
        return result
    }

    companion object {

        internal fun read(type: Int, reader: ConfigReader) = when (type) {
            TYPE_INT -> reader.int()
            else -> throw IllegalArgumentException("Unknown type $type")
        }

        internal fun default(type: Int) = when (type) {
            TYPE_INT -> 0
            else -> throw IllegalArgumentException("Unknown type $type")
        }

        internal fun type(name: String) = when (name) {
            "int" -> TYPE_INT
            else -> throw IllegalArgumentException("Unknown type $name")
        }

        internal const val TYPE_INT = 0
    }
}