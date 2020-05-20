package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Size

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface TargetStrategy {
    fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean
}