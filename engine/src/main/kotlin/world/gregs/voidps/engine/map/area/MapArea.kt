package world.gregs.voidps.engine.map.area

data class MapArea(
    val name: String,
    val area: Area,
    val tags: Set<String>
) {
    companion object {
        val EMPTY = MapArea("", Rectangle(0, 0, 0, 0), emptySet())

        fun fromMap(name: String, map: Map<String, Any>): MapArea {
            val area = map["area"] as Map<String, Any>
            val x = area["x"] as List<Int>
            val y = area["y"] as List<Int>
            val plane = area["plane"] as? Int ?: 0
            val shape = when {
                x.size <= 2 -> Cuboid(x.first(), y.first(), x.last(), y.last(), plane)
                else -> {
                    Polygon(x.toIntArray(), y.toIntArray(), plane)
                }
            }
            return MapArea(
                name = name,
                area = shape,
                tags = (map["tags"] as? List<String>)?.toSet() ?: emptySet()
            )
        }
    }
}