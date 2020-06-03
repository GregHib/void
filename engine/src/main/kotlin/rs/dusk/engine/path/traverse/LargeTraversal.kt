package rs.dusk.engine.path.traverse

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.map.collision.*
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.MediumTraversal.Companion.getNorthCorner
import rs.dusk.engine.path.traverse.MediumTraversal.Companion.getSouthCorner

/**
 * Checks for collision in the direction of movement for entities with any size
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class LargeTraversal(override val type: TraversalType, collidesWithEntities: Boolean, val size: Size, private val collisions: Collisions) : TraversalStrategy {

    override val extra = if(collidesWithEntities) CollisionFlag.ENTITY else 0

    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        val delta = direction.delta
        val inverse = direction.inverse()
        var offsetX = if (delta.x == 1) size.width else delta.x
        var offsetY = if (delta.y == 1) size.height else delta.y
        if (inverse.isCardinal()) {
            // Start
            if (collisions.check(x + offsetX, y + offsetY, plane, getNorthCorner(inverse).block())) {
                return true
            }
            // End
            offsetX = if (delta.x == -1) -1 else size.width + (delta.x - 1)
            offsetY = if (delta.y == -1) -1 else size.height + (delta.y - 1)
            if (collisions.check(x + offsetX, y + offsetY, plane, getSouthCorner(inverse).block())) {
                return true
            }
            // In between
            val s = if (delta.y == 0) size.height else size.width
            for (offset in 1 until s - 1) {
                offsetX = if (delta.x == 1) size.width else if (delta.x == -1) -1 else offset
                offsetY = if (delta.y == 1) size.height else if (delta.y == -1) -1 else offset
                if (collisions.check(x + offsetX, y + offsetY, plane, inverse.not())) {
                    return true
                }
            }
        } else {
            // Diagonal
            if (collisions.check(x + offsetX, y + offsetY, plane, inverse.block())) {
                return true
            }
            // Vertical
            for (offset in 1 until size.width) {
                val dx = offset - if (delta.x == 1) 0 else 1
                if (collisions.check(x + dx, y + offsetY, plane, inverse.vertical().not())) {
                    return true
                }
            }
            // Horizontal
            for (offset in 1 until size.height) {
                val dy = offset - if (delta.y == 1) 0 else 1
                if (collisions.check(x + offsetX, y + dy, plane, inverse.horizontal().not())) {
                    return true
                }
            }
        }

        return false
    }
}