package rs.dusk.engine.path.find

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.entity.index.Steps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.Finder
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy

/**
 * Moves horizontally and vertically until blocked by obstacle or reaches target
 * Used for combat.
 * @author Major
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
class DirectSearch : Finder {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        return addHorizontal(movement.steps, tile, size, strategy, traversal)
    }

    fun addHorizontal(
        steps: Steps,
        tile: Tile,
        size: Size,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        val delta = tile.delta(strategy.tile)
        var dx = delta.x
        var x = tile.x

        if (dx > 0) {
            while (!traversal.blocked(x, tile.y, tile.plane, Direction.WEST) && dx-- > 0) {
                steps.add(Direction.WEST)
                x--
            }
        } else if (dx < 0) {
            while (!traversal.blocked(x, tile.y, tile.plane, Direction.EAST) && dx++ < 0) {
                steps.add(Direction.EAST)
                x++
            }
        }

        val last = tile.copy(x = x)
        return if (strategy.reached(last, size)) {
            PathResult.Success.Complete(last)
        } else if (delta.y != 0 && !traversal.blocked(last, if (delta.y > 0) Direction.SOUTH else Direction.NORTH)) {
            addVertical(steps, last, size, strategy, traversal)
        } else {
            PathResult.Success.Partial(last)
        }
    }

    fun addVertical(
        steps: Steps,
        tile: Tile,
        size: Size,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        val delta = tile.delta(strategy.tile)
        var dy = delta.y
        var y = tile.y

        if (dy > 0) {
            while (!traversal.blocked(tile.x, y, tile.plane, Direction.SOUTH) && dy-- > 0) {
                steps.add(Direction.SOUTH)
                y--
            }
        } else if (dy < 0) {
            while (!traversal.blocked(tile.x, y, tile.plane, Direction.NORTH) && dy++ < 0) {
                steps.add(Direction.NORTH)
                y++
            }
        }

        val last = tile.copy(y = y)
        return if (strategy.reached(last, size)) {
            PathResult.Success.Complete(last)
        } else if (delta.x != 0 && !traversal.blocked(last, if (delta.x > 0) Direction.WEST else Direction.EAST)
        ) {
            addHorizontal(steps, last, size, strategy, traversal)
        } else {
            PathResult.Success.Partial(last)
        }
    }
}