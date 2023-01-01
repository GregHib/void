package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld

/**
 * Checks for collision in the direction of movement for entities of size 2x2
 */
object MediumTraversal : TileTraversalStrategy {

    override fun blocked(collision: CollisionStrategyOld, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean {
        if (direction == Direction.NONE) {
            return collision.blocked(x, y, plane, direction)
                    || collision.blocked(x + 1, y, plane, direction)
                    || collision.blocked(x, y + 1, plane, direction)
                    || collision.blocked(x + 1, y + 1, plane, direction)
        }
        val delta = direction.delta
        val inverse = direction.inverse()
        var offsetX = if (delta.x == 1) size.width else delta.x
        var offsetY = if (delta.y == 1) size.height else delta.y
        if (inverse.isCardinal()) {
            // Start
            if (collision.blocked(x + offsetX, y + offsetY, plane, getNorthCorner(inverse))) {
                return true
            }
            // End
            offsetX = if (delta.x == 0) 1 else if (delta.x == 1) size.width else -1
            offsetY = if (delta.y == 0) 1 else if (delta.y == 1) size.height else -1
            if (collision.blocked(x + offsetX, y + offsetY, plane, getSouthCorner(inverse))) {
                return true
            }
        } else {
            // Diagonal
            if (collision.blocked(x + offsetX, y + offsetY, plane, inverse)) {
                return true
            }
            // Vertical
            val dx = if (delta.x == -1) 0 else delta.x
            if (collision.free(x + dx, y + offsetY, plane, direction.vertical())) {
                return true
            }
            // Horizontal
            val dy = if (delta.y == -1) 0 else delta.y
            if (collision.free(x + offsetX, y + dy, plane, direction.horizontal())) {
                return true
            }
        }

        return false
    }

    fun getNorthCorner(direction: Direction): Direction {
        return when (direction) {
            Direction.EAST -> Direction.NORTH_EAST
            Direction.WEST -> Direction.NORTH_WEST
            Direction.NORTH -> Direction.NORTH_EAST
            Direction.SOUTH -> Direction.SOUTH_EAST
            else -> Direction.NONE
        }
    }

    fun getSouthCorner(direction: Direction): Direction {
        return when (direction) {
            Direction.EAST -> Direction.SOUTH_EAST
            Direction.WEST -> Direction.SOUTH_WEST
            Direction.NORTH -> Direction.NORTH_WEST
            Direction.SOUTH -> Direction.SOUTH_WEST
            else -> Direction.NONE
        }
    }

}