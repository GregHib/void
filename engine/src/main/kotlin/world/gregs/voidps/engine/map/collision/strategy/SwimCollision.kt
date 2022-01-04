package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check

/**
 * Swimming in water but not through entities
 */
object SwimCollision : CollisionStrategy {
    override fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return !collisions.check(x, y, plane, CollisionFlag.WATER) || collisions.check(x, y, plane, CollisionFlag.FLOOR)
    }
}