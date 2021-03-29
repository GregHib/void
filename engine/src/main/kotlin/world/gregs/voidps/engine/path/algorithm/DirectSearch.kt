package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import java.util.*

/**
 * Moves horizontally and vertically until blocked by obstacle or reaches target
 * Used for combat.
 * @author Major
 * @author GregHib <greg@gregs.world>
 * @since May 20, 2020
 */
class DirectSearch : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy
    ): PathResult {
        return addHorizontal(movement.steps, tile, size, strategy, traversal)
    }

    fun addHorizontal(
        steps: LinkedList<Direction>,
        tile: Tile,
        size: Size,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy
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
            PathResult.Success(last)
        } else if (delta.y != 0 && !traversal.blocked(last, if (delta.y > 0) Direction.SOUTH else Direction.NORTH)) {
            addVertical(steps, last, size, strategy, traversal)
        } else {
            PathResult.Partial(last)
        }
    }

    fun addVertical(
        steps: LinkedList<Direction>,
        tile: Tile,
        size: Size,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy
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
            PathResult.Success(last)
        } else if (delta.x != 0 && !traversal.blocked(last, if (delta.x > 0) Direction.WEST else Direction.EAST)
        ) {
            addHorizontal(steps, last, size, strategy, traversal)
        } else {
            PathResult.Partial(last)
        }
    }
}