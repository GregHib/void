package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class InterfaceDefinition(
    override var id: Int = -1,
    var components: MutableMap<Int, InterfaceComponentDefinition>? = null,
    var type: String? = null,
    var fixed: Int = -1,
    var resizable: Int = -1,
    var permanent: Boolean = true,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {

    fun parent(resizable: Boolean) = if (resizable) this.resizable else fixed

    companion object {
        fun id(packed: Int) = packed shr 16
        fun componentId(packed: Int) = packed and 0xffff
        fun pack(id: Int, component: Int) = (id shl 16) or component
        val EMPTY = InterfaceDefinition()
    }
}