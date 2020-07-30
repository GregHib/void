package rs.dusk.engine.path

import rs.dusk.engine.entity.Size
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface TargetStrategy {
    val tile: Tile
    val size: Size

    fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean

    fun reached(tile: Tile, size: Size) = reached(tile.x, tile.y, tile.plane, size)
}