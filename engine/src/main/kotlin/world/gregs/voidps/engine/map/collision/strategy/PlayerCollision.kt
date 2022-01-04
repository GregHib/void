package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Walking on land and through entities
 */
object PlayerCollision : CollisionStrategy {
    override fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagAnd() or CollisionFlag.BLOCKED)
    }

    override fun free(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagNotAnd() or CollisionFlag.BLOCKED)
    }
}