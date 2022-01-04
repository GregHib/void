package world.gregs.voidps.engine.map.collision.strategy

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Water without entities that has at least one full side against land
 */
object ShoreCollision : CollisionStrategy {
    
    override fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        if (SwimCollision.blocked(collisions, x, y, plane, direction)) {
            return true
        }
        if (SwimCollision.blocked(collisions, x, y, plane, direction) && PlayerCollision.blocked(collisions, x, y, plane, direction)) {
            return true
        }
        if (isLand(collisions, x, y, plane, Direction.NORTH)) {
            return isLand(collisions, x, y, plane, Direction.WEST) || isLand(collisions, x, y, plane, Direction.EAST)
        }

        if (isLand(collisions, x, y, plane, Direction.SOUTH)) {
            return isLand(collisions, x, y, plane, Direction.WEST) || isLand(collisions, x, y, plane, Direction.EAST)
        }

        if (isLand(collisions, x, y, plane, Direction.WEST)) {
            return isLand(collisions, x, y, plane, Direction.NORTH) || isLand(collisions, x, y, plane, Direction.SOUTH)
        }

        if (isLand(collisions, x, y, plane, Direction.EAST)) {
            return isLand(collisions, x, y, plane, Direction.NORTH) || isLand(collisions, x, y, plane, Direction.SOUTH)
        }
        return true
    }

    private fun isLand(collisions: Collisions, x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        return !PlayerCollision.free(collisions, x + direction.delta.x, y + direction.delta.y, plane, Direction.NONE) &&
                !PlayerCollision.free(collisions, x + direction.delta.x, y + direction.delta.y, plane, direction.inverse())
    }
}