package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class InterfaceComponentDefinition(
    override var id: Int = -1,
    var options: Array<String?>? = null,
    var information: Array<Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {

    val parent: Int
        get() = InterfaceDefinition.id(id)

    val index: Int
        get() = InterfaceDefinition.componentId(id)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceComponentDefinition

        if (id != other.id) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false
        if (information != null) {
            if (other.information == null) return false
            if (!information.contentEquals(other.information)) return false
        } else if (other.information != null) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + (information?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }
}