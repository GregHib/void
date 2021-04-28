package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import kotlin.random.Random

open class Rectangle(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    val plane: Int = 0
) : Area {

    constructor(tile: Tile, size: Size) : this(tile.x, tile.y, tile.x + size.width, tile.y + size.height, tile.plane)

    override val area = ((maxX - minX) * (maxY - minY)).toDouble()
    override val regions: Set<Region> = calcRegions()

    private fun calcRegions(): Set<Region> {
        val min = Tile(minX, minY).region
        val max = Tile(maxX, maxY).region
        val delta = max.delta(min)
        return min.area(delta.x + 1, delta.y + 1).toSet()
    }

    override fun contains(tile: Tile): Boolean {
        return tile.plane == plane && tile.x >= minX && tile.x <= maxX && tile.y >= minY && tile.y <= maxY
    }

    override fun random() = Tile(if (minX == maxX) minX else Random.nextInt(minX, maxX), if (minY == maxY) minY else Random.nextInt(minY, maxY), plane)

}