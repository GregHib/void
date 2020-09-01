package rs.dusk.engine.path.algorithm

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathAlgorithm
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.engine.path.TraversalStrategy
import kotlin.math.max
import kotlin.math.min

/**
 * Searches every tile breadth-first to find the target
 * Closest reachable tile to target is returned if target is unreachable
 * Used by players
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
class BreadthFirstSearch : PathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        for (x in 0 until GRAPH_SIZE) {
            for (y in 0 until GRAPH_SIZE) {
                movement.directions[x][y] = null
                movement.distances[x][y] = 99999999
            }
        }
        val graph = GRAPH_SIZE / 2
        val graphBaseX = tile.x - graph
        val graphBaseY = tile.y - graph

        var result = calculate(graphBaseX, graphBaseY, tile.plane, size, movement, strategy, traversal)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(movement, strategy, graphBaseX, graphBaseY)
        }

        return when (result) {
            is PathResult.Failure -> result
            is PathResult.Success -> backtrace(movement, result, graphBaseX, graphBaseY)
        }
    }

    fun calculate(
        graphBaseX: Int,
        graphBaseY: Int,
        plane: Int,
        size: Size,
        movement: Movement,
        target: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        // Cache fields for jit compiler performance boost
        val directions = movement.directions
        val distances = movement.distances
        val all = all

        val queue = movement.calc
        queue.clear()

        // Set starting tile as visited
        queue.add(start)
        distances[start.x][start.y] = 0
        directions[start.x][start.y] = Direction.NONE

        var parent: Tile
        while (queue.isNotEmpty()) {
            parent = queue.poll()

            if (target.reached(parent.x + graphBaseX, parent.y + graphBaseY, plane, size)) {
                return PathResult.Success.Complete(parent)
            }

            for (dir in all) {
                val moved = parent.add(dir.delta)

                if (moved.x !in 0 until GRAPH_SIZE) {
                    continue
                }

                if (moved.y !in 0 until GRAPH_SIZE) {
                    continue
                }

                // Skip already calculated steps
                if (directions[moved.x][moved.y] != null) {
                    continue
                }

                // Skip blocked tiles
                if (traversal.blocked(parent.x + graphBaseX, parent.y + graphBaseY, plane, dir)) {
                    continue
                }

                queue.add(moved)
                directions[moved.x][moved.y] = dir
                distances[moved.x][moved.y] = distances[parent.x][parent.y] + 1
            }
        }
        return PathResult.Failure
    }

    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(movement: Movement, target: TargetStrategy, graphBaseX: Int, graphBaseY: Int): PathResult {
        var lowestCost = Integer.MAX_VALUE
        var lowestDistance = Integer.MAX_VALUE
        val distances = movement.distances

        val destX = target.tile.x - graphBaseX
        val destY = target.tile.y - graphBaseY
        var endX = 0
        var endY = 0
        val width = target.size.width
        val height = target.size.height

        val minX = max(0, destX - PARTIAL_PATH_RANGE)
        val maxX = min(GRAPH_SIZE, destX + PARTIAL_PATH_RANGE)
        val minY = max(0, destY - PARTIAL_PATH_RANGE)
        val maxY = min(GRAPH_SIZE, destY + PARTIAL_PATH_RANGE)
        for (graphX in minX..maxX) {
            for (graphY in minY..maxY) {
                if (distances[graphX][graphY] >= PARTIAL_MAX_DISTANCE) {
                    continue
                }

                val deltaX = when {
                    destX > graphX -> destX - graphX// West
                    destX + width <= graphX -> -(destX + width) + graphX + 1// East
                    else -> 0
                }
                val deltaY = when {
                    destY > graphY -> destY - graphY// North
                    destY + height <= graphY -> -(destY + height) + graphY + 1// South
                    else -> 0
                }
                val cost = deltaX * deltaX + deltaY * deltaY
                // Accept lower costs or shorter paths
                if (cost < lowestCost || cost <= lowestCost && distances[graphX][graphY] < lowestDistance) {
                    lowestCost = cost
                    lowestDistance = distances[graphX][graphY]
                    endX = graphX
                    endY = graphY
                }
            }
        }

        if (lowestCost == Integer.MAX_VALUE || lowestDistance == Integer.MAX_VALUE) {
            return PathResult.Failure// No partial path found
        }

        return PathResult.Success.Partial(Tile(endX, endY))
    }

    /**
     *  Traces the path back to find individual steps taken to reach the target
     */
    fun backtrace(movement: Movement, result: PathResult.Success, graphBaseX: Int, graphBaseY: Int): PathResult {
        var trace = result.last
        var direction = movement.directions[trace.x][trace.y]
        val current = movement.steps.count()
        while (direction != null && direction != Direction.NONE && !trace.equals(graphBaseX, graphBaseY)) {
            movement.steps.add(current, direction)
            trace = trace.minus(direction.delta)
            direction = movement.directions[trace.x][trace.y]
        }
        return if(movement.steps.count() == current) {
            PathResult.Failure
        } else {
            result
        }
    }

    companion object {
        const val GRAPH_SIZE = 128
        private const val QUEUE_SIZE = 0xfff
        private const val PARTIAL_MAX_DISTANCE = QUEUE_SIZE
        private const val PARTIAL_PATH_RANGE = 10
        private val start = Tile(GRAPH_SIZE / 2, GRAPH_SIZE / 2)

        private val all = arrayOf(
            Direction.WEST,
            Direction.EAST,
            Direction.SOUTH,
            Direction.NORTH,
            Direction.SOUTH_WEST,
            Direction.SOUTH_EAST,
            Direction.NORTH_WEST,
            Direction.NORTH_EAST
        )
    }
}