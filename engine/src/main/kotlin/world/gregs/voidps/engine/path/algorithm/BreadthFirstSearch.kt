package world.gregs.voidps.engine.path.algorithm

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
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
    private val collisions: Collisions
) : TilePathAlgorithm {
    val mapSize: Int = 128
    private val visits: IntArray = IntArray(mapSize * mapSize)
    private var visit = 0
    private val queueX: IntArray = IntArray(4096 + 256)
    private val queueY: IntArray = IntArray(4096 + 256)
    private var writeIndex = 0
    private var readIndex = 0
    private var start = Tile.EMPTY

    fun start(tile: Tile) {
        reset()
        start = tile.minus(mapSize / 2, mapSize / 2)
        visit(tile, 0, 0)
    }

    var highest = 0

    private fun reset() {
        visit = visit + 1 and 0x3fff
        writeIndex = 0
        readIndex = 0
    }

    fun queue(x: Int, y: Int) {
        queueX[writeIndex] = x
        queueY[writeIndex++] = y
    }

    fun isNotEmpty() = readIndex < writeIndex

    fun peekX() = queueX[readIndex]
    fun peekY() = queueY[readIndex]
    fun next() = readIndex++

    fun visit(x: Int, y: Int, cost: Int, dir: Int): Boolean {
        if (!outOfBounds(x, y)) {
            queueX[writeIndex] = x
            queueY[writeIndex++] = y
            visits[index(x, y)] = pack(cost, visit, dir)
        }
        return true
    }

    fun visit(tile: Tile, cost: Int, dir: Int): Boolean {
        if (outOfBounds(tile.x, tile.y)) {
            return true
        }
        queue(tile.x, tile.y)
        visits[index(tile.x, tile.y)] = pack(cost, visit, dir)
        return true
    }

    fun visited(tile: Tile, default: Boolean): Boolean {
        if (outOfBounds(tile.x, tile.y)) {
            return false
        }
        val index = index(tile.x, tile.y)
        if (index < 0 || index >= visits.size) {
            return default
        }
        return getVisit(visits[index]) == visit
    }

    fun cost(tile: Tile) = cost(tile.x, tile.y)

    fun cost(x: Int, y: Int): Int {
        if (outOfBounds(x, y)) {
            return 0
        }
        return getCost(get(x, y))
    }

    fun visited(x: Int, y: Int): Boolean {
        if (outOfBounds(x, y)) {
            return false
        }
        return getVisit(get(x, y)) == visit
    }

    fun direction(tile: Tile) = direction(tile.x, tile.y)

    fun direction(x: Int, y: Int): Direction = Direction.values[getDir(get(x, y))]

    private fun get(x: Int, y: Int) = visits[index(x, y)]

    private fun index(x: Int, y: Int) = (x - start.x) + ((y - start.y) * mapSize)

    private fun outOfBounds(x: Int, y: Int) = x < start.x || y < start.y

    override fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy
    ): PathResult {
//        val frontier = pool.borrow()
        start(tile)

        var result = calculate(size, path.strategy, traversal, collision, tile.plane)

        if (result is PathResult.Failure) {
            result = calculatePartialPath(tile, path.strategy)
        }

        result = when (result) {
            is PathResult.Failure -> result
            is PathResult.Partial -> backtrace(path, result, result.last, tile)
            is PathResult.Success -> backtrace(path, result, result.last, tile)
        }
//        pool.recycle(frontier)
        return result
    }

    fun calculate(
//        frontier: BreadthFirstSearchFrontier,
        size: Size,
        target: TileTargetStrategy,
        traversal: TileTraversalStrategy,
        collision: CollisionStrategy,
        plane: Int
    ): PathResult {
        var x = 0
        var y = 0
        var parentX = 0
        var parentY = 0
        val collisions = collisions
        val queueX = queueX
        val queueY = queueY
        val visit = visit
        val visits = visits
        var cost = 0
        while (isNotEmpty()) {
            parentX = queueX[readIndex]
            parentY = queueY[readIndex]
            val parent = Tile(parentX, parentY)
            next()

            if (target.reached(parent, size)) {
                return PathResult.Success(parent)
            }

            cost = getCost(get(parentX, parentY))
            if (cost(parent) >= MAX_PATH_COST) {
                break
            }

            x = parentX - 1
            y = parentY
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359560)) {
                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 7)
                }
            }
            x = parentX + 1
            y = parentY
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359680)) {
                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 3)
                }
            }
            x = parentX
            y = parentY - 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359554)) {
                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 5)
                }
            }
            x = parentX
            y = parentY + 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359584)) {
                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 1)
                }
            }
            x = parentX - 1
            y = parentY - 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359566) && !collisions.check(x, parentY, plane, 2359554) && !collisions.check(parentX, y, plane, 2359560)) {

                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 6)
                }
            }
            x = parentX + 1
            y = parentY - 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359683) && !collisions.check(x, parentY, plane, 2359554) && !collisions.check(parentX, y, plane, 2359680)) {

                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 4)
                }
            }
            x = parentX - 1
            y = parentY + 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359608) && !collisions.check(x, parentY, plane, 2359584) && !collisions.check(parentX, y, plane, 2359560)) {

                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 0)
                }
            }
            x = parentX + 1
            y = parentY + 1
            if (!visited(x, y) && !collisions.check(x, y, plane, 2359776) && !collisions.check(x, parentY, plane, 2359584) && !collisions.check(parentX, y, plane, 2359680)) {

                if (!outOfBounds(x, y)) {
                    queueX[writeIndex] = x
                    queueY[writeIndex++] = y
                    visits[index(x, y)] = pack(cost + 1, visit, 2)
                }
            }
        }
        return PathResult.Failure
    }


    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(tile: Tile, target: TileTargetStrategy): PathResult {
        val graph = mapSize / 2
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
        val maxX = min(mapSize, destX + PARTIAL_PATH_RANGE)
        val minY = max(0, destY - PARTIAL_PATH_RANGE)
        val maxY = min(mapSize, destY + PARTIAL_PATH_RANGE)
        for (graphX in minX until maxX) {
            for (graphY in minY until maxY) {
                if (!visited(graphBaseX + graphX, graphBaseY + graphY)) {
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
                if (distance < lowestDist || (distance == lowestDist && cost(graphBaseX + graphX, graphBaseY + graphY) < lowestCost)) {
                    lowestDist = distance
                    lowestCost = cost(graphBaseX + graphX, graphBaseY + graphY)
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
    fun backtrace(path: Path, result: PathResult, last: Tile, tile: Tile): PathResult {
        var trace = last
        var direction = direction(trace)
        if (trace.plane != tile.plane) {
            return PathResult.Failure
        }
        while (trace != tile && visited(trace, false)) {
            path.steps.addFirst(direction)
            trace = trace.minus(direction.delta)
            direction = direction(trace)
        }
        return result
    }

    companion object {
        private fun getDir(value: Int) = value shr 28

        private fun getCost(value: Int) = value shr 14 and 0x3fff

        private fun getVisit(value: Int) = value and 0x3fff

        private fun pack(cost: Int, visit: Int, dir: Int) = visit or (cost shl 14) or (dir shl 28)
        public const val NONE: Int = 0
        public const val NORTH: Int = 0x1
        public const val EAST: Int = 0x2
        public const val SOUTH: Int = 0x4
        public const val WEST: Int = 0x8
        public const val SOUTH_WEST: Int = WEST or SOUTH
        public const val NORTH_WEST: Int = WEST or NORTH
        public const val SOUTH_EAST: Int = EAST or SOUTH
        public const val NORTH_EAST: Int = EAST or NORTH
        const val MAX_PATH_COST = 64
        private const val PARTIAL_PATH_RANGE = 10
    }
}