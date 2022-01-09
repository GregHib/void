package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.RetreatAlgorithm.Companion.STEP_LIMIT
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

/**
 * Creates a path in the opposite direction from a target until blocked or [STEP_LIMIT] reached
 */
class RetreatAlgorithm : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        val delta = path.strategy.tile.delta(tile)
        val direction = when {
            delta.isDiagonal() -> delta.toDirection().inverse()
            delta == Delta.EMPTY -> Direction.SOUTH_WEST
            else -> delta.toDirection().rotate(3)
        }
        var current = tile
        var moved = false
        var count = 0
        while (!path.strategy.reached(current, size) && count++ < STEP_LIMIT) {
            if (traversal.blocked(collision, current, size, direction)) {
                if (!traversal.blocked(collision, current, size, direction.horizontal())) {
                    current = current.add(direction.horizontal())
                    path.steps.add(direction.horizontal())
                    moved = true
                    continue
                } else if (!traversal.blocked(collision, current, size, direction.vertical())) {
                    current = current.add(direction.vertical())
                    path.steps.add(direction.vertical())
                    moved = true
                    continue
                }
                break
            }
            current = current.add(direction.delta)
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