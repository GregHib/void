package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.path.TraversalType

/**
 * Checks for collision in the direction of movement for entities of size 1x1
 * If direction of movement is diagonal then both horizontal and vertical directions are checked too.
 *
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
class SmallTraversal(private val type: TraversalType, collidesWithEntities: Boolean, private val collisions: Collisions) : TileTraversalStrategy {

    private val extra = if(collidesWithEntities) CollisionFlag.ENTITY else 0

    // Motion (land, sky, ignored), entities y/n
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        val inverse = direction.inverse()
        if (collisions.check(
                x + direction.delta.x,
                y + direction.delta.y,
                plane,
                inverse.block(type, extra)
            )
        ) {
            return true
        }
        if (!direction.isDiagonal()) {
            return false
        }
        // Horizontal
        if (collisions.check(x + direction.delta.x, y, plane, inverse.horizontal().block(type, extra))) {
            return true
        }
        // Vertical
        if (collisions.check(x, y + direction.delta.y, plane, inverse.vertical().block(type, extra))) {
            return true
        }
        return false
    }
}