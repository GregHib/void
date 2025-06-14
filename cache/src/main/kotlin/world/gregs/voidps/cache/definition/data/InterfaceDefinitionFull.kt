package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class InterfaceDefinitionFull(
    override var id: Int = -1,
    var components: Array<InterfaceComponentDefinitionFull>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Extra {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceDefinitionFull

        if (id != other.id) return false
        if (components != null) {
            if (other.components == null) return false
            if (!components.contentEquals(other.components)) return false
        } else if (other.components != null) {
            return false
        }
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (components?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun id(packed: Int) = packed shr 16
        fun componentId(packed: Int) = packed and 0xffff
        fun pack(id: Int, component: Int) = (id shl 16) or component
        val EMPTY = InterfaceDefinitionFull()
    }
}
