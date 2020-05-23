package rs.dusk.engine.path.find

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.entity.index.Steps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.block
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.path.Finder
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
class AxisAlignment(private val collisions: Collisions) : Finder {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        val delta = strategy.tile.delta(tile)
        val direction = toDirection(delta)
        return horizontal(movement.steps, tile, size, direction.horizontal(), direction.vertical(), strategy)
    }

    fun horizontal(
        steps: Steps,
        tile: Tile,
        size: Size,
        horizontal: Direction,
        vertical: Direction,
        strategy: TargetStrategy
    ): PathResult {
        var offset = 0
        var reached = strategy.reached(tile.x, tile.y, tile.plane, size)
        if (horizontal != Direction.NONE) {
            while (!reached) {
                if (collisions.check(tile.x + offset, tile.y, tile.plane, horizontal.block())) {
                    break
                }
                offset += horizontal.delta.x
                steps.add(horizontal)
                reached = strategy.reached(tile.x + offset, tile.y, tile.plane, size)
            }
        }
        val last = tile.add(x = offset)
        return if (reached) {
            PathResult.Success.Complete(last)
        } else {
            if (vertical != Direction.NONE &&
                !collisions.check(last.x, last.y + vertical.delta.y, last.plane, vertical.block())
            ) {
                vertical(steps, last, size, horizontal, vertical, strategy)
            } else {
                PathResult.Success.Partial(last)
            }
        }
    }

    fun vertical(
        steps: Steps,
        tile: Tile,
        size: Size,
        horizontal: Direction,
        vertical: Direction,
        strategy: TargetStrategy
    ): PathResult {
        var offset = 0
        var reached = strategy.reached(tile.x, tile.y, tile.plane, size)
        if (vertical != Direction.NONE) {
            while (!reached) {
                if (collisions.check(tile.x, tile.y + offset, tile.plane, vertical.block())) {
                    break
                }
                offset += vertical.delta.y
                steps.add(vertical)
                reached = strategy.reached(tile.x, tile.y + offset, tile.plane, size)
            }
        }
        val last = tile.add(y = offset)
        return if (reached) {
            PathResult.Success.Complete(last)
        } else {
            if (horizontal != Direction.NONE &&
                !collisions.check(last.x + horizontal.delta.x, last.y, last.plane, horizontal.block())
            ) {
                horizontal(steps, last, size, horizontal, vertical, strategy)
            } else {
                PathResult.Success.Partial(last)
            }
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