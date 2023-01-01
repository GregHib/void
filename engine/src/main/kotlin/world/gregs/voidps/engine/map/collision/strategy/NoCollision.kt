package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Walking on land and through entities
 */
class NoCollision(
    collisions: Collisions
) : CollisionStrategyOld(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return false
    }

    override fun free(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return true
    }
}