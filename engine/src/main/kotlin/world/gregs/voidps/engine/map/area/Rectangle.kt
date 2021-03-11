package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.map.Tile
import kotlin.random.Random

open class Rectangle(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    val plane: Int = 0
) : Area {

    override val area = ((maxX - minX) * (maxY - minY)).toDouble()

    override fun contains(tile: Tile): Boolean {
        return tile.plane == plane && tile.x >= minX && tile.x <= maxX && tile.y >= minY && tile.y <= maxY
    }

    override fun random() = Tile(Random.nextInt(minX, maxX), Random.nextInt(minY, maxY), plane)

}