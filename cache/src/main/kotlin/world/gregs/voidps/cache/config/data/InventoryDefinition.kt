package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class InventoryDefinition(
    override var id: Int = -1,
    var length: Int = 0,
    var ids: IntArray? = null,
    var amounts: IntArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InventoryDefinition

        if (id != other.id) return false
        if (length != other.length) return false
        if (ids != null) {
            if (other.ids == null) return false
            if (!ids.contentEquals(other.ids)) return false
        } else if (other.ids != null) {
            return false
        }
        if (amounts != null) {
            if (other.amounts == null) return false
            if (!amounts.contentEquals(other.amounts)) return false
        } else if (other.amounts != null) {
            return false
        }
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + length
        result = 31 * result + (ids?.contentHashCode() ?: 0)
        result = 31 * result + (amounts?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + extras.hashCode()
        return result
    }

    companion object {
        val EMPTY = InventoryDefinition()
    }
}
