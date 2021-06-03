package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.path.TraversalType

/**
 * Tiles in the water alongside land which doesn't have an entity on
 */
class ShoreTraversal(private val collisions: Collisions) : TileTraversalStrategy {

    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean {
        if (collisions.check(x, y, plane, CollisionFlag.ENTITY) || isLand(x, y, plane, Direction.NONE)) {
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

    private fun isLand(x: Int, y: Int, plane: Int, direction: Direction) = !collisions.check(x + direction.delta.x, y + direction.delta.y, plane, Direction.NONE.block(TraversalType.Land))
}