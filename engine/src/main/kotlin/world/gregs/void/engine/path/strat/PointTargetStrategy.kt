package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.item.FloorItem
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * Checks if within reachable range of a tile
 * e.g floor item on a tile or table
 * Note: Doesn't check if blocked
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
data class PointTargetStrategy(
    private val floorItem: FloorItem
) : TargetStrategy {

    override val tile: Tile
        get() = floorItem.tile

    override val size: Size
        get() = floorItem.size

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        if (tile.x + size.width <= currentX || tile.x >= currentX + size.width) {
            return false
        }
        return currentY < tile.y + size.height && size.height + currentY > tile.y
    }
}