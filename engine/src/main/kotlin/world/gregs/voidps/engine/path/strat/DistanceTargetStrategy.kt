package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.TargetStrategy

/**
 * Checks if within distance of a target
 * @author GregHib <greg@gregs.world>
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