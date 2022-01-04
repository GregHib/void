package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Can pass through bank booths and border guards
 */
object IgnoredCollision : CollisionStrategy {
    override fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagAnd() shl 22 or CollisionFlag.IGNORED)
    }

    override fun free(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagNotAnd() shl 22 or CollisionFlag.IGNORED)
    }
}