package rs.dusk.engine.path.traverse

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.map.collision.CollisionFlag
import rs.dusk.engine.model.map.collision.Collisions
import rs.dusk.engine.model.map.collision.check
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.TraversalType

/**
 * Checks for collision in the direction of movement for entities of size 2x2
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class MediumTraversal(override val type: TraversalType, collidesWithEntities: Boolean, private val collisions: Collisions) : TraversalStrategy {

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
            offsetX = if (delta.x == 0) 1 else if (delta.x == 1) size.width else -1
            offsetY = if (delta.y == 0) 1 else if (delta.y == 1) size.height else -1
            if (collisions.check(x + offsetX, y + offsetY, plane, getSouthCorner(inverse).block())) {
                return true
            }
        } else {
            // Diagonal
            if (collisions.check(x + offsetX, y + offsetY, plane, inverse.block())) {
                return true
            }
            // Vertical
            val dx = if (delta.x == -1) 0 else delta.x
            if (collisions.check(x + dx, y + offsetY, plane, direction.vertical().not())) {
                return true
            }
            // Horizontal
            val dy = if (delta.y == -1) 0 else delta.y
            if (collisions.check(x + offsetX, y + dy, plane, direction.horizontal().not())) {
                return true
            }
        }

        return false
    }

    companion object {
        private val size = Size(2, 2)
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
}