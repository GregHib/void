package world.gregs.voidps.engine.map.area

import world.gregs.voidps.cache.definition.Extra

data class MapArea(
    val name: String,
    val area: Area,
    val tags: Set<String>,
    override var stringId: String = name,
    override var extras: Map<String, Any>? = null
) : Extra {
    companion object {
        val EMPTY = MapArea("", Rectangle(0, 0, 0, 0), emptySet())

        fun fromMap(name: String, map: Map<String, Any>): MapArea {
            val extras = map.toMutableMap()
            extras.remove("area")
            extras.remove("tags")
            return MapArea(
                name = name,
                area = map["area"] as Area,
                tags = (map["tags"] as? Set<String>) ?: emptySet(),
                extras = extras
            )
        }
    }
}