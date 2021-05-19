package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals

/**
 * Checks if on an exact tile
 */
data class SingleTileTargetStrategy(
    override val tile: Tile,
    override val size: Size = Size.TILE
) : TileTargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.equals(currentX, currentY, plane)
    }
}