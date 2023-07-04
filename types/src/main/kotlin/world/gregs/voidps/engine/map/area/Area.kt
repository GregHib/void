package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.zone.Zone

interface Area {
    val area: Double

    operator fun contains(tile: Tile): Boolean = contains(tile.x, tile.y, tile.plane)

    fun contains(x: Int, y: Int, plane: Int = 0): Boolean

    fun random(): Tile

    fun toRegions(): List<Region>

    fun toZones(plane: Int = 0): List<Zone>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>, maxPlane: Int): Area {
            val x = map["x"] as List<Int>
            val y = map["y"] as List<Int>
            val plane = map["plane"] as? Int
            return if (x.size <= 2) {
                Cuboid(x.first(), y.first(), x.last(), y.last(), plane ?: 0, plane ?: maxPlane)
            } else {
                Polygon(x.toIntArray(), y.toIntArray(), plane ?: 0, plane ?: maxPlane)
            }
        }
    }
}