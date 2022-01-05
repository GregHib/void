package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check

/**
 * Land is blocked and water is free
 */
class LandCollision(
    collisions: Collisions
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return !collisions.check(x + direction.delta.x, y + direction.delta.y, plane, CollisionFlag.WATER) || collisions.check(x + direction.delta.x, y + direction.delta.y, plane, CollisionFlag.FLOOR)
    }
}