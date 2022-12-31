package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.flag

/**
 * Walking anywhere on land and through entities
 */
class LandCollision(
    collisions: Collisions
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.flag())// or CollisionFlag.BLOCKED)
    }

    override fun free(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return !collisions.check(x, y, plane, direction.flag())
    }
}