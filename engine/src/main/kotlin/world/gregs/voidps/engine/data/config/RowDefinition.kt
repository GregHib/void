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

    val itemId: String get() = stringId.substringAfterLast('.')

    fun bool(column: String) = Tables.bool("${stringId}.$column")

    fun boolOrNull(column: String) = Tables.boolOrNull("${stringId}.$column")

    fun int(column: String) = Tables.int("${stringId}.$column")

    fun skillPair(column: String) = Tables.skillPair("${stringId}.$column")

    fun skillPairOrNull(column: String) = Tables.skillPairOrNull("${stringId}.$column")

    fun intList(column: String) = Tables.intList("${stringId}.$column")

    fun intListOrNull(column: String) = Tables.intListOrNull("${stringId}.$column")

    fun intOrNull(column: String) = Tables.intOrNull("${stringId}.$column")

    fun intRange(column: String) = Tables.intRange("${stringId}.$column")

    fun intRangeOrNull(column: String) = Tables.intRangeOrNull("${stringId}.$column")

    fun itemPair(column: String) = Tables.itemPair("${stringId}.$column")

    fun string(column: String) = Tables.string("${stringId}.$column")

    fun stringOrNull(column: String) = Tables.stringOrNull("${stringId}.$column")

    fun item(column: String) = Tables.item("${stringId}.$column")

    fun itemOrNull(column: String) = Tables.itemOrNull("${stringId}.$column")

    fun itemList(column: String) = Tables.itemList("${stringId}.$column")

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