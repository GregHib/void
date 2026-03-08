package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Rectangle

data class AreaDefinition(
    val name: String,
    val area: Area,
    val tags: Set<String> = emptySet(),
    override var stringId: String = name,
    override var params: Map<Int, Any>? = null,
) : Parameterized {
    companion object {
        val EMPTY = AreaDefinition("", Rectangle(0, 0, 0, 0), emptySet())
    }
}
