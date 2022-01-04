package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionStrategy

/**
 * Checks for collision in the direction of movement for entities of size 1x1
 * If direction of movement is diagonal then both horizontal and vertical directions are checked too.
 */
object SmallTraversal : TileTraversalStrategy {

    override fun blocked(collision: CollisionStrategy, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean {
        if (direction == Direction.NONE) {
            return collision.blocked(x, y, plane, direction)
        }
        val inverse = direction.inverse()
        if (collision.blocked(x + direction.delta.x, y + direction.delta.y, plane, inverse)) {
            return true
        }
        if (!direction.isDiagonal()) {
            return false
        }
        // Horizontal
        if (collision.blocked(x + direction.delta.x, y, plane, inverse.horizontal())) {
            return true
        }
        // Vertical
        if (collision.blocked(x, y + direction.delta.y, plane, inverse.vertical())) {
            return true
        }
        return false
    }
}