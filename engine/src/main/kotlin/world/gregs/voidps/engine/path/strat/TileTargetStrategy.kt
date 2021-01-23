package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.TargetStrategy

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