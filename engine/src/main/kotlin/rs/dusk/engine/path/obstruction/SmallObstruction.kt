package rs.dusk.engine.path.obstruction

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.block
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.path.ObstructionStrategy

/**
 * Checks for collision in the direction of movement for entities of size 1x1
 * If direction of movement is diagonal then both horizontal and vertical directions are checked too.
 *
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class SmallObstruction(private val collisions: Collisions) : ObstructionStrategy {

    override fun obstructed(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        if (collisions.check(
                x + direction.delta.x,
                y + direction.delta.y,
                plane,
                direction.inverse().block()
            )
        ) {
            return true
        }
        if (!direction.isDiagonal()) {
            return false
        }
        // Horizontal
        if (collisions.check(x + direction.delta.x, y, plane, direction.horizontal().block())) {
            return true
        }
        // Vertical
        if (collisions.check(x, y + direction.delta.y, plane, direction.vertical().block())) {
            return true
        }
        return false
    }
}