package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.AvoidAlgorithm.Companion.STEP_LIMIT
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

/**
 * Creates a path in the opposite direction from a target until blocked or [STEP_LIMIT] reached
 */
class AvoidAlgorithm : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        var delta = tile.delta(path.strategy.tile)
        var current = tile

        var count = 0
        var moved = false
        while (count-- < STEP_LIMIT) {
            val direction = delta.toDirection()
            if (direction == Direction.NONE || traversal.blocked(collision, current, size, direction)) {
                break
            }
            current = current.add(direction.delta)
            delta = current.delta(path.strategy.tile)
            path.steps.add(direction)
            moved = true
        }

        return when {
            moved -> PathResult.Success(current)
            else -> PathResult.Failure
        }
    }

    companion object {
        private const val STEP_LIMIT = 10
    }
}