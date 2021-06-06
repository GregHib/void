package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import kotlin.random.Random

data class Rectangle(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
) : Area, Iterable<Tile> {

    constructor(tile: Tile, width: Int, height: Int) : this(tile.x, tile.y, tile.x + width, tile.y + height)

    override val area: Double
        get() = (width * height).toDouble()
    val width: Int
        get() = maxX - minX
    val height: Int
        get() = maxY - minY

    override fun toRegions(): List<Region> {
        val list = mutableListOf<Region>()
        val max = Tile(maxX, maxY).region
        val min = Tile(minX, minY).region
        for (x in min.x..max.x) {
            for (y in min.y..max.y) {
                list.add(Region(x, y))
            }
        }
        return list
    }

    override fun toChunks(): List<Chunk> {
        val list = mutableListOf<Chunk>()
        val max = Tile(maxX, maxY).chunk
        val min = Tile(minX, minY).chunk
        for (x in min.x..max.x) {
            for (y in min.y..max.y) {
                list.add(Chunk(x, y))
            }
        }
        return list
    }

    override fun contains(x: Int, y: Int, plane: Int): Boolean {
        return x in minX..maxX && y in minY..maxY
    }

    override fun random() = Tile(if (minX == maxX) minX else Random.nextInt(minX, maxX + 1), if (minY == maxY) minY else Random.nextInt(minY, maxY + 1), 0)


    override fun toString(): String {
        return "Rectangle($minX..$maxX, $minY..$maxY)"
    }

    override fun iterator(): Iterator<Tile> {
        val tile = Tile(minX, minY)
        return object : Iterator<Tile> {
            private var index = 0

            override fun hasNext() = index < area

            override fun next() = tile.add(x = index / height, y = index++.rem(height))
        }
    }
}