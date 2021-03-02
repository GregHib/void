package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if within reachable range of a tile
 * e.g floor item on a tile or table
 * Note: Doesn't check if blocked
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
data class PointTargetStrategy(
    private val floorItem: FloorItem
) : TileTargetStrategy {

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