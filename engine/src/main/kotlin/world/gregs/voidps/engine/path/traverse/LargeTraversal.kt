package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.MediumTraversal.Companion.getNorthCorner
import world.gregs.voidps.engine.path.traverse.MediumTraversal.Companion.getSouthCorner

/**
 * Checks for collision in the direction of movement for entities with any size
 */
class LargeTraversal(private val type: TraversalType, collidesWithEntities: Boolean, val size: Size, private val collisions: Collisions) : TileTraversalStrategy {

    private val extra = if (collidesWithEntities) CollisionFlag.ENTITY else 0

    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        if (direction == Direction.NONE) {
            for (w in 0 until size.width) {
                for (h in 0 until size.height) {
                    if (collisions.check(x + w, y + h, plane, direction.block(type, extra))) {
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
            if (collisions.check(x + offsetX, y + offsetY, plane, getNorthCorner(inverse).block(type, extra))) {
                return true
            }
            // End
            offsetX = if (delta.x == -1) -1 else size.width + (delta.x - 1)
            offsetY = if (delta.y == -1) -1 else size.height + (delta.y - 1)
            if (collisions.check(x + offsetX, y + offsetY, plane, getSouthCorner(inverse).block(type, extra))) {
                return true
            }
            // In between
            val s = if (delta.y == 0) size.height else size.width
            for (offset in 1 until s - 1) {
                offsetX = if (delta.x == 1) size.width else if (delta.x == -1) -1 else offset
                offsetY = if (delta.y == 1) size.height else if (delta.y == -1) -1 else offset
                if (collisions.check(x + offsetX, y + offsetY, plane, direction.not(type, extra))) {
                    return true
                }
            }
        } else {
            // Diagonal
            if (collisions.check(x + offsetX, y + offsetY, plane, inverse.block(type, extra))) {
                return true
            }
            // Vertical
            for (offset in 1 until size.width) {
                val dx = offset - if (delta.x == 1) 0 else 1
                if (collisions.check(x + dx, y + offsetY, plane, direction.vertical().not(type, extra))) {
                    return true
                }
            }
            // Horizontal
            for (offset in 1 until size.height) {
                val dy = offset - if (delta.y == 1) 0 else 1
                if (collisions.check(x + offsetX, y + dy, plane, direction.horizontal().not(type, extra))) {
                    return true
                }
            }
        }

        return false
    }
}