package rs.dusk.cache.definition.data

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class WorldMapSection(val plane: Int, val minX: Int, val minY: Int, val maxX: Int, val maxY: Int, var startX: Int, var startY: Int, var endX: Int, var endY: Int)