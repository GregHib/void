package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld
import world.gregs.voidps.engine.path.traverse.MediumTraversal.getNorthCorner
import world.gregs.voidps.engine.path.traverse.MediumTraversal.getSouthCorner

/**
 * Checks for collision in the direction of movement for entities with any size
 */
object LargeTraversal : TileTraversalStrategy {

    override fun blocked(collision: CollisionStrategyOld, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean {
        if (direction == Direction.NONE) {
            for (w in 0 until size.width) {
                for (h in 0 until size.height) {
                    if (collision.blocked(x + w, y + h, plane, direction)) {
                        return true
                    }
                }
            }
            return false
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
            offsetX = if (delta.x == -1) -1 else size.width + (delta.x - 1)
            offsetY = if (delta.y == -1) -1 else size.height + (delta.y - 1)
            if (collision.blocked(x + offsetX, y + offsetY, plane, getSouthCorner(inverse))) {
                return true
            }
            // In between
            val s = if (delta.y == 0) size.height else size.width
            for (offset in 1 until s - 1) {
                offsetX = if (delta.x == 1) size.width else if (delta.x == -1) -1 else offset
                offsetY = if (delta.y == 1) size.height else if (delta.y == -1) -1 else offset
                if (collision.free(x + offsetX, y + offsetY, plane, direction)) {
                    return true
                }
            }
        } else {
            // Diagonal
            if (collision.blocked(x + offsetX, y + offsetY, plane, inverse)) {
                return true
            }
            // Vertical
            for (offset in 1 until size.width) {
                val dx = offset - if (delta.x == 1) 0 else 1
                if (collision.free(x + dx, y + offsetY, plane, direction.vertical())) {
                    return true
                }
            }
            // Horizontal
            for (offset in 1 until size.height) {
                val dy = offset - if (delta.y == 1) 0 else 1
                if (collision.free(x + offsetX, y + dy, plane, direction.horizontal())) {
                    return true
                }
            }
        }

        return false
    }
}