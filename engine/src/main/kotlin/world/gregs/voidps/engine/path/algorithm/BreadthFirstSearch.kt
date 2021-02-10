package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.path.PathAlgorithm
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import kotlin.math.max
import kotlin.math.min

class Discovery(val mapSize: Int = 128){

    private val start = Tile(mapSize / 2, mapSize / 2)
    private val discovered: IntArray = IntArray(mapSize * mapSize)
    private val queue: IntArray = IntArray(mapSize * mapSize)
    private var visit: Int = 0
    var writeIndex: Int = 0

    fun start() = visit(start, 0, 0)

    fun queue(tile: Tile) {
        queue[writeIndex++] = tile.id
    }

    fun poll(index: Int): Tile = Tile(queue[index])

    fun visit(tile: Tile, cost: Int, dir: Int) {
        queue(tile)
        discovered[index(tile.x, tile.y)] = pack(cost, visit, dir)
    }

    fun cost(tile: Tile) = cost(tile.x, tile.y)

    fun cost(x: Int, y: Int): Int = getCost(discovered[index(x, y)])

    fun visited(tile: Tile) = visited(tile.x, tile.y)

    fun inBounds(tile: Tile) = inBounds(tile.x, tile.y)

    fun inBounds(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0 || x >= mapSize || y >= mapSize) {
            return false
        }
        return true
    }

    fun visited(x: Int, y: Int): Boolean = getVisit(discovered[index(x, y)]) == visit

    fun direction(tile: Tile) = direction(tile.x, tile.y)

    fun direction(x: Int, y: Int): Int = getDir(discovered[index(x, y)])

    fun index(x: Int, y: Int) = x + (y * mapSize)

    fun reset() {
        visit++
        visit = visit and 0x3fff
        writeIndex = 0
    }

    companion object {
        fun getDir(value: Int) = value shr 28
        fun getCost(value: Int) = value shr 14 and 0x3fff
        fun getVisit(value: Int) = value and 0x3fff
        fun pack(cost: Int, visit: Int, dir: Int) = visit or (cost shl 14) or (dir shl 28)
    }
}

/**
 * Searches every tile breadth-first to find the target
 * Closest reachable tile to target is returned if target is unreachable
 * Used by players
 * @author GregHib <greg@gregs.world>
 * @since May 20, 2020
 */
class BreadthFirstSearch : PathAlgorithm {
    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy,
    ): PathResult {
        val discovery = movement.discovery
        discovery.reset()
        val graph = GRAPH_SIZE / 2
        val graphBaseX = tile.x - graph
        val graphBaseY = tile.y - graph

        var result = calculate(discovery, graphBaseX, graphBaseY, tile.plane, size, strategy, traversal)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(strategy, graphBaseX, graphBaseY, discovery)
        }

        return when (result) {
            is PathResult.Failure -> result
            is PathResult.Partial -> backtrace(movement, discovery, result, result.last)
            is PathResult.Success -> backtrace(movement, discovery, result, result.last)
        }
    }

    fun calculate(
        discovery: Discovery,
        graphBaseX: Int,
        graphBaseY: Int,
        plane: Int,
        size: Size,
        target: TargetStrategy,
        traversal: TraversalStrategy,
    ): PathResult {
        discovery.start()
        var readIndex = 0
        var parent: Tile
        while (readIndex < discovery.writeIndex) {
            parent = discovery.poll(readIndex++)

            if (target.reached(parent.x + graphBaseX, parent.y + graphBaseY, plane, size)) {
                return PathResult.Success(parent)
            }

            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.WEST)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.EAST)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.SOUTH)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.NORTH)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.SOUTH_WEST)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.SOUTH_EAST)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.NORTH_WEST)
            check(discovery, parent, traversal, graphBaseX, graphBaseY, plane, Direction.NORTH_EAST)

        }
        return PathResult.Failure
    }

    private fun check(
        discovery: Discovery,
        parent: Tile,
        traversal: TraversalStrategy,
        graphBaseX: Int,
        graphBaseY: Int,
        plane: Int,
        dir: Direction,
    ) {
        val moved = parent.add(dir.delta)

        if (!discovery.inBounds(moved)) {
            return
        }

        // Skip already calculated steps
        if (discovery.visited(moved)) {
            return
        }

        // Skip blocked tiles
        if (traversal.blocked(parent.x + graphBaseX, parent.y + graphBaseY, plane, dir)) {
            return
        }

        discovery.visit(moved, discovery.cost(parent) + 1, dir.ordinal and 0x7)
    }

    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(target: TargetStrategy, graphBaseX: Int, graphBaseY: Int, discovery: Discovery): PathResult {
        var lowestCost = Integer.MAX_VALUE
        var lowestDistance = Integer.MAX_VALUE

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
                if (!discovery.visited(graphX, graphY)) {
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
                if (cost < lowestCost || (cost == lowestCost && discovery.cost(graphX, graphY) < lowestDistance)) {
                    lowestCost = cost
                    lowestDistance = discovery.cost(graphX, graphY)
                    endX = graphX
                    endY = graphY
                }
            }
        }

        if (lowestCost == Integer.MAX_VALUE || lowestDistance == Integer.MAX_VALUE) {
            return PathResult.Failure// No partial path found
        }

        return PathResult.Partial(Tile(endX, endY))
    }

    /**
     *  Traces the path back to find individual steps taken to reach the target
     */
    fun backtrace(movement: Movement, discovery: Discovery, result: PathResult, last: Tile): PathResult {
        var trace = last
        var direction = Direction.values[discovery.direction(trace)]
        val current = movement.steps.count()
        while (!trace.equals(64, 64)) {
            movement.steps.add(current, direction)
            trace = trace.minus(direction.delta)
            direction = Direction.values[discovery.direction(trace)]
        }
        return if (movement.steps.count() == current) {
            PathResult.Failure
        } else {
            result
        }
    }

    companion object {
        const val GRAPH_SIZE = 128
        private const val PARTIAL_PATH_RANGE = 10
        private val start = Tile(GRAPH_SIZE / 2, GRAPH_SIZE / 2)
    }
}