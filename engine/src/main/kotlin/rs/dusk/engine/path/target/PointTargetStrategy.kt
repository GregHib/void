package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.path.Target
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class PointTargetStrategy : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean {
        if (target.tile.x + target.size.width <= currentX || target.tile.x >= currentX + size.width) {
            return false
        }
        return currentY < target.tile.y + target.size.height && size.height + currentY > target.tile.y
    }
}