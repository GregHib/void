package world.gregs.voidps.engine.data.config

import world.gregs.voidps.engine.data.definition.Tables

/**
 * DbRow
 */
data class RowDefinition(
    val id: Int,
    val data: Array<Any?>,
    val stringId: String
) {

    fun int(column: String) = Tables.int("${stringId}.$column")

    fun intOrNull(column: String) = Tables.intOrNull("${stringId}.$column")

    fun string(column: String) = Tables.string("${stringId}.$column")

    fun stringOrNull(column: String) = Tables.stringOrNull("${stringId}.$column")

    fun item(column: String) = Tables.item("${stringId}.$column")

    fun itemOrNull(column: String) = Tables.itemOrNull("${stringId}.$column")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RowDefinition

        if (!data.contentEquals(other.data)) return false
        if (stringId != other.stringId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + stringId.hashCode()
        return result
    }
}