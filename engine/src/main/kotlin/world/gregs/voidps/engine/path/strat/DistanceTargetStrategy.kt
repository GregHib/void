package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if within distance of a target
 */
data class DistanceTargetStrategy(
    private var distance: Int,
    override val tile: Tile,
    override val size: Size = Size.ONE
) : TileTargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.within(currentX, currentY, plane, distance)
    }
}