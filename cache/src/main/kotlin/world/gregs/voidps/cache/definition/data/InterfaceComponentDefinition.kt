package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class InterfaceComponentDefinition(
    override var id: Int = -1,
    var options: Array<String?>? = null,
    var anObjectArray4758: Array<Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceComponentDefinition

        if (id != other.id) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false
        if (anObjectArray4758 != null) {
            if (other.anObjectArray4758 == null) return false
            if (!anObjectArray4758.contentEquals(other.anObjectArray4758)) return false
        } else if (other.anObjectArray4758 != null) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + (anObjectArray4758?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }
}