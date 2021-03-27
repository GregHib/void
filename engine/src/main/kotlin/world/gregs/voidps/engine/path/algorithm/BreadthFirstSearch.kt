package world.gregs.voidps.engine.path.algorithm

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import kotlin.math.max
import kotlin.math.min

/**
 * Searches every tile breadth-first to find the target
 * Closest reachable tile to target is returned if target is unreachable
 * Used by players
 * @author GregHib <greg@gregs.world>
 * @since May 20, 2020
 */
class BreadthFirstSearch(
    private val pool: ObjectPool<BreadthFirstSearchFrontier>,
) : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy,
    ): PathResult {
        val frontier = pool.borrow()
        frontier.start(tile)

        var result = calculate(frontier, size, strategy, traversal)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(frontier, tile, strategy)
        }

        result = when (result) {
            is PathResult.Failure -> result
            is PathResult.Partial -> backtrace(movement, frontier, result, result.last, tile)
            is PathResult.Success -> backtrace(movement, frontier, result, result.last, tile)
        }
        pool.recycle(frontier)
        return result
    }

    fun calculate(
        frontier: BreadthFirstSearchFrontier,
        size: Size,
        target: TileTargetStrategy,
        traversal: TileTraversalStrategy,
    ): PathResult {
        while (frontier.isNotEmpty()) {
            val parent = frontier.poll()

            if (target.reached(parent, size)) {
                return PathResult.Success(parent)
            }

            if (frontier.cost(parent) >= MAX_PATH_COST) {
                break
            }

            check(frontier, traversal, parent, Direction.WEST)
            check(frontier, traversal, parent, Direction.EAST)
            check(frontier, traversal, parent, Direction.SOUTH)
            check(frontier, traversal, parent, Direction.NORTH)
            check(frontier, traversal, parent, Direction.SOUTH_WEST)
            check(frontier, traversal, parent, Direction.SOUTH_EAST)
            check(frontier, traversal, parent, Direction.NORTH_WEST)
            check(frontier, traversal, parent, Direction.NORTH_EAST)
        }
        return PathResult.Failure
    }

    private fun check(frontier: BreadthFirstSearchFrontier, traversal: TileTraversalStrategy, parent: Tile, dir: Direction) {
        val tile = parent.add(dir.delta)
        if (frontier.visited(tile, true) || traversal.blocked(parent, dir)) {
            return
        }
        frontier.visit(tile, frontier.cost(parent) + 1, dir.ordinal and 0x7)
    }

    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(frontier: BreadthFirstSearchFrontier, tile: Tile, target: TileTargetStrategy): PathResult {
        val graph = frontier.mapSize / 2
        val graphBaseX = tile.x - graph
        val graphBaseY = tile.y - graph
        var lowestDist = Integer.MAX_VALUE
        var lowestCost = Integer.MAX_VALUE

        val destX = target.tile.x - graphBaseX
        val destY = target.tile.y - graphBaseY
        var endX = 0
        var endY = 0
        val width = target.size.width
        val height = target.size.height

        val minX = max(0, destX - PARTIAL_PATH_RANGE)
        val maxX = min(frontier.mapSize, destX + PARTIAL_PATH_RANGE)
        val minY = max(0, destY - PARTIAL_PATH_RANGE)
        val maxY = min(frontier.mapSize, destY + PARTIAL_PATH_RANGE)
        for (graphX in minX until maxX) {
            for (graphY in minY until maxY) {
                if (!frontier.visited(graphBaseX + graphX, graphBaseY + graphY)) {
                    continue
                }

                val deltaX = when {
                    destX > graphX -> destX - graphX// West
                    destX + width <= graphX -> graphX + 1 - (destX + width)// East
                    else -> 0
                }
                val deltaY = when {
                    destY > graphY -> destY - graphY// North
                    destY + height <= graphY -> graphY + 1 - (destY + height)// South
                    else -> 0
                }
                val distance = deltaX * deltaX + deltaY * deltaY// Euclidean
                // Accept lower costs or shorter paths
                if (distance < lowestDist || (distance == lowestDist && frontier.cost(graphBaseX + graphX, graphBaseY + graphY) < lowestCost)) {
                    lowestDist = distance
                    lowestCost = frontier.cost(graphBaseX + graphX, graphBaseY + graphY)
                    endX = graphBaseX + graphX
                    endY = graphBaseY + graphY
                }
            }
        }

        if (lowestDist == Integer.MAX_VALUE || lowestCost == Integer.MAX_VALUE) {
            return PathResult.Failure// No partial path found
        }

        return PathResult.Partial(Tile(endX, endY))
    }

    /**
     *  Traces the path back to find individual steps taken to reach the target
     */
    fun backtrace(movement: Movement, frontier: BreadthFirstSearchFrontier, result: PathResult, last: Tile, tile: Tile): PathResult {
        var trace = last
        var direction = frontier.direction(trace)
        movement.steps.clear()
        if (trace.plane != tile.plane) {
            return PathResult.Failure
        }
        while (trace != tile && frontier.visited(trace, false)) {
            movement.steps.addFirst(direction)
            trace = trace.minus(direction.delta)
            direction = frontier.direction(trace)
        }
        return result
    }

    companion object {
        const val MAX_PATH_COST = 64
        private const val PARTIAL_PATH_RANGE = 10
    }
}