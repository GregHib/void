package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionStrategy

/**
 * Swimming in water but not through entities
 */
object SwimTraversal : TileTraversalStrategy {
    override fun blocked(collision: CollisionStrategy, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean {
        if (direction.isDiagonal() && (collision.blocked(x, y, plane, direction.horizontal()) || collision.blocked(x, y, plane, direction.vertical()))) {
            return true
        }
        return collision.blocked(x, y, plane, direction)
    }
}