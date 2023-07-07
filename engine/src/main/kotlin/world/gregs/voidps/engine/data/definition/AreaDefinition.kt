package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Rectangle

data class AreaDefinition(
    val name: String,
    val area: Area,
    val tags: Set<String>,
    override var stringId: String = name,
    override var extras: Map<String, Any>? = null
) : Extra {
    companion object {
        val EMPTY = AreaDefinition("", Rectangle(0, 0, 0, 0), emptySet())

        fun fromMap(name: String, map: Map<String, Any>): AreaDefinition {
            val extras = map.toMutableMap()
            extras.remove("area")
            extras.remove("tags")
            return AreaDefinition(
                name = name,
                area = map["area"] as Area,
                tags = (map["tags"] as? Set<String>) ?: emptySet(),
                extras = extras
            )
        }
    }
}