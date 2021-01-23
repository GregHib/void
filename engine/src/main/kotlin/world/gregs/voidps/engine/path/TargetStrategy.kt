package world.gregs.voidps.engine.path

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

/**
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
interface TargetStrategy {
    val tile: Tile
    val size: Size

    fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean

    fun reached(tile: Tile, size: Size) = reached(tile.x, tile.y, tile.plane, size)
}