package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.TargetStrategy

interface TileTargetStrategy : TargetStrategy {
    val tile: Tile
    val size: Size

    fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean = false

    fun reached(tile: Tile, size: Size) = reached(tile.x, tile.y, tile.plane, size)
}