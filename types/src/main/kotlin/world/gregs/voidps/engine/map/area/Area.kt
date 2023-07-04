package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.zone.Zone

interface Area {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.level)

    fun contains(x: Int, y: Int, level: Int = 0): Boolean

    fun random(): Tile

    fun toRegions(): List<Region>

    fun toZones(level: Int = 0): List<Zone>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>, maxLevel: Int): Area {
            val x = map["x"] as List<Int>
            val y = map["y"] as List<Int>
            val level = map["level"] as? Int
            return if (x.size <= 2) {
                Cuboid(x.first(), y.first(), x.last(), y.last(), level ?: 0, level ?: maxLevel)
            } else {
                Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: maxLevel)
            }
        }
    }
}