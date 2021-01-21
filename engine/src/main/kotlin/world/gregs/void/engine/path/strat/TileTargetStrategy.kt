package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * Checks if on an exact tile
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
data class TileTargetStrategy(
    override val tile: Tile,
    override val size: Size = Size.TILE
) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.equals(currentX, currentY, plane)
    }
}