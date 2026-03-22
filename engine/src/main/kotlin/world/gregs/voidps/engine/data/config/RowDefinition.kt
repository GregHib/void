package world.gregs.voidps.engine.data.config

/**
 * DbRow
 */
data class RowDefinition(
    val data: Array<Any?>,
    val stringId: String
) {
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