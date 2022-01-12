package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Can pass through bank booths and border guards
 */
class IgnoredCollision(
    collisions: Collisions,
    private val land: LandCollision
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return land.blocked(x, y, plane, direction) && !collisions.check(x, y, plane, direction.and() shl 22 or CollisionFlag.IGNORED)
    }
}