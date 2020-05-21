package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface TargetStrategy {
    val tile: Tile
    val size: Size

    fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean
}