package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.path.Target
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class TileTargetStrategy : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean {
        return target.tile.equals(currentX, currentY, plane)
    }
}