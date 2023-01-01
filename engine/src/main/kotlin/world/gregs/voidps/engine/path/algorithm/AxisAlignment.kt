package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Overlap.isUnder
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

/**
 * Moves diagonally until aligned with target or blocked by obstacle then moves cardinally
 * Used by NPCs
 */
class AxisAlignment : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategyOld
    ): PathResult {
        var delta = path.strategy.tile.delta(tile)
        var current = tile
        var reached = path.strategy.reached(current, size)

        fun step(direction: Direction) {
            delta = delta.minus(direction.delta)
            current = current.add(direction.delta)
            path.steps.add(direction)
            reached = path.strategy.reached(current, size)
        }

        // Step out if stuck under
        if (!reached && isUnder(tile, size, path.strategy.tile, path.strategy.size)) {
            var valid: Direction = Direction.NONE
            for (direction in Direction.cardinal) {
                if (!traversal.blocked(collision, current, size, direction)) {
                    valid = direction
                    if (!isUnder(current.add(direction), size, path.strategy.tile, path.strategy.size)) {
                        step(direction)
                        valid = Direction.NONE
                        break
                    }
                }
            }
            if (valid != Direction.NONE) {
                step(valid)
            }
        }

        // Align axis
        while (!reached) {
            var direction = delta.toDirection()
            if (traversal.blocked(collision, current, size, direction)) {
                direction = if (direction.isDiagonal()) {
                    if (!traversal.blocked(collision, current, size, direction.horizontal())) {
                        direction.horizontal()
                    } else if (!traversal.blocked(collision, current, size, direction.vertical())) {
                        direction.vertical()
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            if (direction == Direction.NONE) {
                break
            }
            step(direction)
        }

        return when {
            reached -> PathResult.Success(current)
            current != tile -> PathResult.Partial(current)
            else -> PathResult.Failure
        }
    }
}