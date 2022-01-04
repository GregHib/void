package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import java.util.*

/**
 * Moves horizontally and vertically until blocked by obstacle or reaches target
 * Used for combat.
 * @author Major
 */
class DirectSearch : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        return addHorizontal(path.steps, tile, size, path.strategy, traversal, collision)
    }

    fun addHorizontal(
        steps: LinkedList<Direction>,
        tile: Tile,
        size: Size,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        val delta = tile.delta(strategy.tile)
        var dx = delta.x
        var x = tile.x

        if (dx > 0) {
            while (!traversal.blocked(collision, x, tile.y, tile.plane, size, Direction.WEST) && dx-- > 0) {
                steps.add(Direction.WEST)
                x--
            }
        } else if (dx < 0) {
            while (!traversal.blocked(collision, x, tile.y, tile.plane, size, Direction.EAST) && dx++ < 0) {
                steps.add(Direction.EAST)
                x++
            }
        }

        val last = tile.copy(x = x)
        return if (strategy.reached(last, size)) {
            PathResult.Success(last)
        } else if (delta.y != 0 && !traversal.blocked(collision, last, size, if (delta.y > 0) Direction.SOUTH else Direction.NORTH)) {
            addVertical(steps, last, size, strategy, traversal, collision)
        } else {
            PathResult.Partial(last)
        }
    }

    fun addVertical(
        steps: LinkedList<Direction>,
        tile: Tile,
        size: Size,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        val delta = tile.delta(strategy.tile)
        var dy = delta.y
        var y = tile.y

        if (dy > 0) {
            while (!traversal.blocked(collision, tile.x, y, tile.plane, size, Direction.SOUTH) && dy-- > 0) {
                steps.add(Direction.SOUTH)
                y--
            }
        } else if (dy < 0) {
            while (!traversal.blocked(collision, tile.x, y, tile.plane, size, Direction.NORTH) && dy++ < 0) {
                steps.add(Direction.NORTH)
                y++
            }
        }

        val last = tile.copy(y = y)
        return if (strategy.reached(last, size)) {
            PathResult.Success(last)
        } else if (delta.x != 0 && !traversal.blocked(collision, last, size, if (delta.x > 0) Direction.WEST else Direction.EAST)) {
            addHorizontal(steps, last, size, strategy, traversal, collision)
        } else {
            PathResult.Partial(last)
        }
    }
}