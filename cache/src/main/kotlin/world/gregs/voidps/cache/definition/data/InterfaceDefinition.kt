package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

@Suppress("ArrayInDataClass")
data class InterfaceDefinition(
    override var id: Int = -1,
    var actualId: Int = -1,
    var components: MutableMap<Int, InterfaceComponentDefinition>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {
    companion object {
        val EMPTY = InterfaceDefinition()
    }
}