package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check

/**
 * Not an entity
 */
class WaterTraversal(private val collisions: Collisions) : TileTraversalStrategy {

    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x + direction.delta.x, y + direction.delta.y, plane, CollisionFlag.ENTITY)
    }
}