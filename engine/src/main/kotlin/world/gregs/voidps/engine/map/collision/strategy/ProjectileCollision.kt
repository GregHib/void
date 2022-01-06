package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Can pass over low bushes and gates but not bank booths
 */
class ProjectileCollision(
    collisions: Collisions
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, CollisionFlag.IGNORED or (direction.and() shl 9 or CollisionFlag.SKY))
    }

    override fun free(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, CollisionFlag.IGNORED or (direction.not() shl 9 or CollisionFlag.SKY))
    }
}