package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Parameterized

data class StructDefinition(
    override var id: Int = -1,
    override var params: Map<Long, Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Parameterized, Extra {
    companion object {
        val EMPTY = StructDefinition()
    }
}