package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * Checks if within distance of a target
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since September 9, 2020
 */
data class DistanceTargetStrategy(
    private var distance: Int,
    override val tile: Tile,
    override val size: Size = Size.TILE
) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        return tile.within(currentX, currentY, plane, distance)
    }
}