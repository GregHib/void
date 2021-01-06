package rs.dusk.engine.path.algorithm

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathAlgorithm
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

/**
 * Moves diagonally until aligned with target or blocked by obstacle then moves cardinally
 * Used by NPCs
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 31, 2020
 */
class AxisAlignment : PathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        var delta = strategy.tile.delta(tile)
        println(delta)
        var current = tile

        var reached = strategy.reached(current, size)
        println(reached)
        while (!reached) {
            var direction = toDirection(delta)
            println("Check $current $direction ${traversal.blocked(current, direction)}")
            if (traversal.blocked(current, direction)) {
                direction = if (direction.isDiagonal()) {
                    println("Check $current ${direction.horizontal()} ${direction.vertical()} ${traversal.blocked(current, direction.horizontal())} ${traversal.blocked(current, direction.vertical())}")
                    if (!traversal.blocked(current, direction.horizontal())) {
                        direction.horizontal()
                    } else if (!traversal.blocked(current, direction.vertical())) {
                        direction.vertical()
                    } else {
                        println("Break")
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
            println("Queue $current $direction")
            movement.steps.add(direction)
            reached = strategy.reached(current, size)
        }

        println("$current $tile")
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