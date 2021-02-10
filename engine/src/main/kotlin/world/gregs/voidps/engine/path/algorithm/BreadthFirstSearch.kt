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

class Discovery(val mapSize: Int = 128) {

    private val discovered: IntArray = IntArray(mapSize * mapSize)
    private val queue: IntArray = IntArray(mapSize * mapSize)
    private var visit: Int = 0
    var writeIndex: Int = 0
    var start = Tile.EMPTY

    fun start(tile: Tile) {
        reset()
        start = tile.minus(mapSize / 2, mapSize / 2)
        visit(tile, 0, 0)
    }

    fun queue(tile: Tile) {
        queue[writeIndex++] = tile.id
    }

    fun poll(index: Int): Tile = Tile(queue[index])

    fun visit(tile: Tile, cost: Int, dir: Int) {
        queue(tile)
        discovered[index(tile.x, tile.y)] = pack(cost, visit, dir)
    }

    fun cost(tile: Tile) = cost(tile.x, tile.y)

    fun cost(x: Int, y: Int): Int = getCost(get(x, y))

    fun visited(tile: Tile, default: Boolean): Boolean {
        val value = discovered.getOrNull(index(tile.x, tile.y)) ?: return default
        return getVisit(value) == visit
    }

    fun visited(x: Int, y: Int): Boolean = getVisit(get(x, y)) == visit

    fun direction(tile: Tile) = direction(tile.x, tile.y)

    fun direction(x: Int, y: Int): Int = getDir(get(x, y))

    private fun get(x: Int, y: Int) = discovered[index(x, y)]

    fun index(x: Int, y: Int) = (x - start.x) + ((y - start.y) * mapSize)

    fun reset() {
        visit = visit + 1 and 0x3fff
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
        discovery.start(tile)

        var result = calculate(discovery, size, strategy, traversal)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(discovery, tile, strategy)
        }

        return when (result) {
            is PathResult.Failure -> result
            is PathResult.Partial -> backtrace(movement, discovery, result, result.last, tile)
            is PathResult.Success -> backtrace(movement, discovery, result, result.last, tile)
        }
    }

    fun calculate(
        discovery: Discovery,
        size: Size,
        target: TargetStrategy,
        traversal: TraversalStrategy,
    ): PathResult {
        var readIndex = 0
        while (readIndex < discovery.writeIndex) {
            val parent = discovery.poll(readIndex++)

            if (target.reached(parent, size)) {
                return PathResult.Success(parent)
            }

            if (discovery.cost(parent) >= MAX_PATH_COST) {
                break
            }

            check(discovery, traversal, parent, Direction.WEST)
            check(discovery, traversal, parent, Direction.EAST)
            check(discovery, traversal, parent, Direction.SOUTH)
            check(discovery, traversal, parent, Direction.NORTH)
            check(discovery, traversal, parent, Direction.SOUTH_WEST)
            check(discovery, traversal, parent, Direction.SOUTH_EAST)
            check(discovery, traversal, parent, Direction.NORTH_WEST)
            check(discovery, traversal, parent, Direction.NORTH_EAST)
        }
        return PathResult.Failure
    }

    private fun check(discovery: Discovery, traversal: TraversalStrategy, parent: Tile, dir: Direction) {
        val tile = parent.add(dir.delta)
        if (discovery.visited(tile, true) || traversal.blocked(parent, dir)) {
            return
        }
        discovery.visit(tile, discovery.cost(parent) + 1, dir.ordinal and 0x7)
    }

    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(discovery: Discovery, tile: Tile, target: TargetStrategy): PathResult {
        val graph = discovery.mapSize / 2
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
        val maxX = min(discovery.mapSize, destX + PARTIAL_PATH_RANGE)
        val minY = max(0, destY - PARTIAL_PATH_RANGE)
        val maxY = min(discovery.mapSize, destY + PARTIAL_PATH_RANGE)
        for (graphX in minX..maxX) {
            for (graphY in minY..maxY) {
                if (!discovery.visited(graphBaseX + graphX, graphBaseY + graphY)) {
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
                if (distance < lowestDist || (distance == lowestDist && discovery.cost(graphBaseX + graphX, graphBaseY + graphY) < lowestCost)) {
                    lowestDist = distance
                    lowestCost = discovery.cost(graphBaseX + graphX, graphBaseY + graphY)
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
    fun backtrace(movement: Movement, discovery: Discovery, result: PathResult, last: Tile, tile: Tile): PathResult {
        var trace = last
        var direction = Direction.values[discovery.direction(trace)]
        val current = movement.steps.count()
        while (trace != tile) {
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
        const val MAX_PATH_COST = 64
        private const val PARTIAL_PATH_RANGE = 10
    }
}