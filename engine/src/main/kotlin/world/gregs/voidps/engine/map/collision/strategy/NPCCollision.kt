package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.*

/**
 * Walking on land but not through npcs or players
 */
class NPCCollision(
    collisions: Collisions
) : CollisionStrategy(collisions) {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.and() or CollisionFlag.BLOCKED or CollisionFlag.ENTITY)
    }

    override fun free(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return collisions.check(x, y, plane, direction.not() or CollisionFlag.BLOCKED or CollisionFlag.ENTITY)
    }
}