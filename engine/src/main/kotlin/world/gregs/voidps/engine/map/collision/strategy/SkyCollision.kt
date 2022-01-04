package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Can pass over low bushes and gates
 */
object SkyCollision : CollisionStrategy {
    override fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagAnd() shl 9 or CollisionFlag.SKY)
    }

    override fun free(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flagNotAnd() shl 9 or CollisionFlag.SKY)
    }
}