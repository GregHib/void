package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized

data class StructDefinition(
    override var id: Int = -1,
    override var stringId: String = "",
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {
    companion object {
        val EMPTY = StructDefinition()
    }
}
