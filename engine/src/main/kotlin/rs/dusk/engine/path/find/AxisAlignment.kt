package rs.dusk.engine.path.find

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.Movement
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.Finder
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

/**
 * Moves diagonally until aligned with target or blocked by obstacle then moves cardinally
 * Used by NPCs
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 31, 2020
 */
class AxisAlignment : Finder {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
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
            reached -> PathResult.Success.Complete(current)
            current != tile -> PathResult.Success.Partial(current)
            else -> PathResult.Failure
        }
    }

    fun toDirection(delta: Tile) = when {
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