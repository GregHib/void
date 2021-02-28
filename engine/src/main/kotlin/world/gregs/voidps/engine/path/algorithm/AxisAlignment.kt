package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

/**
 * Moves diagonally until aligned with target or blocked by obstacle then moves cardinally
 * Used by NPCs
 * @author GregHib <greg@gregs.world>
 * @since May 31, 2020
 */
class AxisAlignment : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy
    ): PathResult {
        var delta = strategy.tile.delta(tile)
        var current = tile

        var reached = strategy.reached(current, size)
        while (!reached) {
            var direction = toDirection(delta)
            if (traversal.blocked(current, direction)) {
                direction = if (direction.isDiagonal()) {
                    if (!traversal.blocked(current, direction.horizontal())) {
                        direction.horizontal()
                    } else if (!traversal.blocked(current, direction.vertical())) {
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
            delta = delta.minus(direction.delta)
            current = current.add(direction.delta)
            movement.steps.add(direction)
            reached = strategy.reached(current, size)
        }

        return when {
            reached -> PathResult.Success(current)
            current != tile -> PathResult.Partial(current)
            else -> PathResult.Failure
        }
    }

    fun toDirection(delta: Delta) = when {
        delta.x > 0 -> when {
            delta.y > 0 -> Direction.NORTH_EAST
            delta.y < 0 -> Direction.SOUTH_EAST
            else -> Direction.EAST
        }
        delta.x < 0 -> when {
            delta.y > 0 -> Direction.NORTH_WEST
            delta.y < 0 -> Direction.SOUTH_WEST
            else -> Direction.WEST
        }
        else -> when {
            delta.y > 0 -> Direction.NORTH
            delta.y < 0 -> Direction.SOUTH
            else -> Direction.NONE
        }
    }
}