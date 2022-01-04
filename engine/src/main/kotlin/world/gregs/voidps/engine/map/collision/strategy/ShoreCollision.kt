package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Water without entities that has at least one full side against land
 */
class ShoreCollision(
    collisions: Collisions,
    private val playerCollision: PlayerCollision,
    private val swimCollision: SwimCollision
) : CollisionStrategy(collisions) {
    
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        if (swimCollision.blocked(x, y, plane, direction)) {
            return true
        }
        if (swimCollision.blocked(x, y, plane, direction) && playerCollision.blocked(x, y, plane, direction)) {
            return true
        }
        if (isLand(x, y, plane, Direction.NORTH)) {
            return isLand(x, y, plane, Direction.WEST) || isLand(x, y, plane, Direction.EAST)
        }

        if (isLand(x, y, plane, Direction.SOUTH)) {
            return isLand(x, y, plane, Direction.WEST) || isLand(x, y, plane, Direction.EAST)
        }

        if (isLand(x, y, plane, Direction.WEST)) {
            return isLand(x, y, plane, Direction.NORTH) || isLand(x, y, plane, Direction.SOUTH)
        }

        if (isLand(x, y, plane, Direction.EAST)) {
            return isLand(x, y, plane, Direction.NORTH) || isLand(x, y, plane, Direction.SOUTH)
        }
        return true
    }

    private fun isLand(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return !playerCollision.free(x + direction.delta.x, y + direction.delta.y, plane, Direction.NONE) &&
                !playerCollision.free(x + direction.delta.x, y + direction.delta.y, plane, direction.inverse())
    }
}