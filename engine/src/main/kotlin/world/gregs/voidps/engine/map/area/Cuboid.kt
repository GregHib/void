package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane
import kotlin.random.Random

data class Cuboid(
    val minX: Int,
    val minY: Int,
    val maxX: Int = minX,
    val maxY: Int = minY,
    val minPlane: Int = 0,
    val maxPlane: Int = minPlane
) : Area, Iterable<Tile> {

    constructor(tile: Tile, width: Int, height: Int, planes: Int) : this(tile.x, tile.y, tile.x + width - 1, tile.y + height - 1, tile.plane, tile.plane + planes - 1)

    override val area: Double
        get() = (width * height * planes).toDouble()

    val width: Int
        get() = maxX - minX + 1

    val height: Int
        get() = maxY - minY + 1

    val planes: Int
        get() = maxPlane - minPlane + 1

    override fun toRegions(): List<Region> {
        val list = mutableListOf<Region>()
        val max = Tile(maxX, maxY, maxPlane).region
        val min = Tile(minX, minY, minPlane).region
        for (x in min.x..max.x) {
            for (y in min.y..max.y) {
                list.add(Region(x, y))
            }
        }
        return list
    }

    override fun toChunks(): List<Chunk> {
        val list = mutableListOf<Chunk>()
        val max = Tile(maxX, maxY, maxPlane).chunk
        val min = Tile(minX, minY, minPlane).chunk
        for (plane in min.plane..max.plane) {
            for (x in min.x..max.x) {
                for (y in min.y..max.y) {
                    list.add(Chunk(x, y, plane))
                }
            }
        }
        return list
    }

    fun toRegionPlanes(): List<RegionPlane> {
        val list = mutableListOf<RegionPlane>()
        val max = Tile(maxX, maxY, maxPlane).regionPlane
        val min = Tile(minX, minY, minPlane).regionPlane
        for (plane in min.plane..max.plane) {
            for (x in min.x..max.x) {
                for (y in min.y..max.y) {
                    list.add(RegionPlane(x, y, plane))
                }
            }
        }
        return list
    }

    fun toRectangles(): List<Rectangle> = (minPlane..maxPlane).map { Rectangle(minX, minY, maxX, maxY) }

    override fun contains(x: Int, y: Int, plane: Int): Boolean {
        return plane in minPlane..maxPlane && x in minX..maxX && y in minY..maxY
    }

    override fun random() = Tile(random(minX, maxX), random(minY, maxY), random(minPlane, maxPlane))

    companion object {
        fun random(first: Int, second: Int) = if (first == second) first else Random.nextInt(first, second + 1)
    }

    override fun toString(): String {
        return "Cuboid($minX..$maxX, $minY..$maxY, $minPlane..$maxPlane)"
    }

    override fun iterator(): Iterator<Tile> {
        val tile = Tile(minX, minY, minPlane)
        return object : Iterator<Tile> {
            private val max = area
            private var index = 0

            override fun hasNext() = index < max

            override fun next(): Tile {
                val coords = tile.add(
                    x = index.rem(width * height) / height,
                    y = index.rem(width * height).rem(height),
                    plane = index / (width * height)
                )
                index++
                return coords
            }
        }
    }
}