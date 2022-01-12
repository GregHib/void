package world.gregs.voidps.engine.path.algorithm

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import kotlin.math.max
import kotlin.math.min

/**
 * Searches every tile breadth-first to find the target
 * The closest reachable tile to the target is returned if the target is unreachable
 * Used by players and nex
 */
class BreadthFirstSearch(
    private val pool: ObjectPool<BreadthFirstSearchFrontier>,
) : TilePathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        val frontier = pool.borrow()
        frontier.start(tile)

        var result = calculate(frontier, size, path.strategy, traversal, collision)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(frontier, tile, path.strategy)
        }

        result = when (result) {
            is PathResult.Failure -> result
            is PathResult.Partial -> backtrace(path, frontier, result, result.last, tile)
            is PathResult.Success -> backtrace(path, frontier, result, result.last, tile)
        }
        pool.recycle(frontier)
        return result
    }

    fun calculate(
        frontier: BreadthFirstSearchFrontier,
        size: Size,
        target: TileTargetStrategy,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
        while (frontier.isNotEmpty()) {
            val parent = frontier.poll()

            if (target.reached(parent, size)) {
                return PathResult.Success(parent)
            }

            if (frontier.cost(parent) >= MAX_PATH_COST) {
                break
            }

            check(frontier, traversal, collision, parent, size, Direction.WEST)
            check(frontier, traversal, collision, parent, size, Direction.EAST)
            check(frontier, traversal, collision, parent, size, Direction.SOUTH)
            check(frontier, traversal, collision, parent, size, Direction.NORTH)
            check(frontier, traversal, collision, parent, size, Direction.SOUTH_WEST)
            check(frontier, traversal, collision, parent, size, Direction.SOUTH_EAST)
            check(frontier, traversal, collision, parent, size, Direction.NORTH_WEST)
            check(frontier, traversal, collision, parent, size, Direction.NORTH_EAST)
        }
        return PathResult.Failure
    }

    private fun check(frontier: BreadthFirstSearchFrontier, traversal: TileTraversalStrategy, collision: CollisionStrategy, parent: Tile, size: Size, dir: Direction) {
        val tile = parent.add(dir.delta)
        if (frontier.visited(tile, true) || traversal.blocked(collision, parent, size, dir)) {
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

        return PathResult.Partial(Tile(endX, endY, tile.plane))
    }

    /**
     *  Traces the path back to find individual steps taken to reach the target
     */
    fun backtrace(path: Path, frontier: BreadthFirstSearchFrontier, result: PathResult, last: Tile, tile: Tile): PathResult {
        var trace = last
        var direction = frontier.direction(trace)
        if (trace.plane != tile.plane) {
            return PathResult.Failure
        }
        while (trace != tile && frontier.visited(trace, false)) {
            path.steps.addFirst(direction)
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