package world.gregs.voidps.cache.definition.data

data class WorldMapSection(
    val level: Int,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    var startX: Int,
    var startY: Int,
    var endX: Int,
    var endY: Int,
)
