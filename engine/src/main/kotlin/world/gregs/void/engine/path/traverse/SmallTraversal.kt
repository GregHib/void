package world.gregs.void.engine.path.traverse

import world.gregs.void.engine.entity.Direction
import world.gregs.void.engine.map.collision.CollisionFlag
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.map.collision.check
import world.gregs.void.engine.path.TraversalStrategy
import world.gregs.void.engine.path.TraversalType

/**
 * Checks for collision in the direction of movement for entities of size 1x1
 * If direction of movement is diagonal then both horizontal and vertical directions are checked too.
 *
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
class SmallTraversal(override val type: TraversalType, collidesWithEntities: Boolean, private val collisions: Collisions) : TraversalStrategy {

    override val extra = if(collidesWithEntities) CollisionFlag.ENTITY else 0

    // Motion (land, sky, ignored), entities y/n
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        val inverse = direction.inverse()
        if (collisions.check(
                x + direction.delta.x,
                y + direction.delta.y,
                plane,
                inverse.block()
            )
        ) {
            return true
        }
        if (!direction.isDiagonal()) {
            return false
        }
        // Horizontal
        if (collisions.check(x + direction.delta.x, y, plane, inverse.horizontal().block())) {
            return true
        }
        // Vertical
        if (collisions.check(x, y + direction.delta.y, plane, inverse.vertical().block())) {
            return true
        }
        return false
    }
}