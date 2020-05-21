package rs.dusk.engine.path.find

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.Finder
import rs.dusk.engine.path.ObstructionStrategy
import rs.dusk.engine.path.TargetStrategy
import rs.dusk.utility.func.nearby

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
class BreadthFirstSearch : Finder {

    private var exitX: Int = -1
    private var exitY: Int = -1
    private var graphBaseX = -1
    private var graphBaseY = -1
    var isPartial: Boolean = false
        private set

    val lastPathBufferX = IntArray(QUEUE_SIZE)
    val lastPathBufferY = IntArray(QUEUE_SIZE)

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        obstruction: ObstructionStrategy
    ): Int {
        isPartial = false
        for (x in 0 until GRAPH_SIZE) {
            for (y in 0 until GRAPH_SIZE) {
                directions[x][y] = 0
                distances[x][y] = 99999999
            }
        }

        graphBaseX = tile.x - GRAPH_SIZE / 2
        graphBaseY = tile.y - GRAPH_SIZE / 2

        val found = calculate(tile, size, strategy, obstruction)

        if (!found && !calculatePartialPath(strategy)) {
            return -1// No path found
        }

        if (exitX == tile.x && exitY == tile.y) {
            return 0// No movement
        }

        return backtrace(tile)
    }

    fun calculate(
        position: Tile,
        size: Size,
        strategy: TargetStrategy,
        obstruction: ObstructionStrategy
    ): Boolean {
        // Cache fields for jit compiler performance boost
        val directions = directions
        val distances = distances
        val bufferX = lastPathBufferX
        val bufferY = lastPathBufferY
        val all = all
        val queueBounds = QUEUE_SIZE - 1
        val graphBounds = GRAPH_SIZE - 1

        // Start in centre
        var currentX = position.x
        var currentY = position.y
        var currentGraphX = GRAPH_SIZE / 2
        var currentGraphY = GRAPH_SIZE / 2

        // Set starting tile as visited
        distances[currentGraphX][currentGraphY] = 0
        directions[currentGraphX][currentGraphY] = 99

        var read = 0
        var write = 0
        // Queue current position
        bufferX[write] = currentX
        bufferY[write++] = currentY

        while (read != write) {
            currentX = bufferX[read]
            currentY = bufferY[read]
            read = read + 1 and queueBounds

            currentGraphX = currentX - graphBaseX
            currentGraphY = currentY - graphBaseY

            // Check if path is complete
            if (strategy.reached(currentX, currentY, position.plane, size)) {
                exitX = currentX
                exitY = currentY
                return true
            }

            // Check for collisions
            for (dir in all) {
                // Check all directions

                // Skip if horizontal out of bounds
                if (dir.delta.x == -1 && currentGraphX <= 0 || dir.delta.x == 1 && currentGraphX >= graphBounds) {
                    continue
                }

                // Skip if vertical out of bounds
                if (dir.delta.y == -1 && currentGraphY <= 0 || dir.delta.y == 1 && currentGraphY >= graphBounds) {
                    continue
                }

                if (directions[currentGraphX + dir.delta.x][currentGraphY + dir.delta.y] == 0 && !obstruction.obstructed(
                        currentX,
                        currentY,
                        position.plane,
                        dir
                    )
                ) {
                    // Set the next step
                    bufferX[write] = currentX + dir.delta.x
                    bufferY[write] = currentY + dir.delta.y
                    // Increase the queue
                    write = write + 1 and queueBounds

                    // Set the direction
                    directions[currentGraphX + dir.delta.x][currentGraphY + dir.delta.y] = getDirectionFlag(dir)
                    // Set the distance
                    distances[currentGraphX + dir.delta.x][currentGraphY + dir.delta.y] =
                        distances[currentGraphX][currentGraphY] + 1
                }
            }
        }

        exitX = currentX
        exitY = currentY
        return false
    }

    /**
     *  Checks for a tile closest to the target which is reachable
     */
    fun calculatePartialPath(target: TargetStrategy): Boolean {
        isPartial = true
        var lowestCost = Integer.MAX_VALUE
        var lowestDistance = Integer.MAX_VALUE

        val destX = target.tile.x
        val destY = target.tile.y
        var endX = exitX
        var endY = exitY
        val width = target.size.width
        val height = target.size.height

        for (checkX in destX.nearby(PARTIAL_PATH_RANGE)) {
            for (checkY in destY.nearby(PARTIAL_PATH_RANGE)) {
                val graphX = checkX - graphBaseX
                val graphY = checkY - graphBaseY
                if (graphX < 0 || graphY < 0 || graphX >= GRAPH_SIZE || graphY >= GRAPH_SIZE || distances[graphX][graphY] >= PARTIAL_MAX_DISTANCE) {
                    continue
                }

                // Calculate deltas using strategy
                val deltaX = if (destX <= checkX) {
                    1 - destX - (width - checkX)
                } else {
                    destX - checkX
                }

                val deltaY = if (destY <= checkY) {
                    1 - destY - (height - checkY)
                } else {
                    destY - checkY
                }

                val cost = deltaX * deltaX + deltaY * deltaY
                if (cost < lowestCost || cost <= lowestCost && distances[graphX][graphY] < lowestDistance) {
                    // Accept lower costs or shorter paths
                    lowestCost = cost
                    lowestDistance = distances[graphX][graphY]
                    endX = checkX
                    endY = checkY
                }
            }
        }

        if (lowestCost == Integer.MAX_VALUE || lowestDistance == Integer.MAX_VALUE) {
            return false// No partial path found
        }

        exitX = endX
        exitY = endY
        return true
    }

    /**
     *  Traces the path back to find how many steps were taken to reach the target
     */
    fun backtrace(position: Tile): Int {
        var steps = 0
        var traceX = exitX
        var traceY = exitY
        var direction = directions[traceX - graphBaseX][traceY - graphBaseY]
        var lastWritten = direction
        val bufferX = lastPathBufferX
        val bufferY = lastPathBufferY
        // Queue destination position and start tracing from it
        bufferX[steps] = traceX
        bufferY[steps++] = traceY
        while (traceX != position.x || traceY != position.y) {
            // Direction changed
            if (lastWritten != direction) {
                bufferX[steps] = traceX
                bufferY[steps++] = traceY
                lastWritten = direction
            }

            if (direction and DIR_WEST != 0) {
                traceX++
            } else if (direction and DIR_EAST != 0) {
                traceX--
            }

            if (direction and DIR_SOUTH != 0) {
                traceY++
            } else if (direction and DIR_NORTH != 0) {
                traceY--
            }

            direction = directions[traceX - graphBaseX][traceY - graphBaseY]
        }
        return steps
    }

    private fun getDirectionFlag(dir: Direction): Int {
        return when (dir.delta.x) {
            1 -> DIR_EAST
            -1 -> DIR_WEST
            else -> 0
        } or when (dir.delta.y) {
            1 -> DIR_NORTH
            -1 -> DIR_SOUTH
            else -> 0
        }
    }

    companion object {
        private const val GRAPH_SIZE = 128
        private const val QUEUE_SIZE = GRAPH_SIZE * GRAPH_SIZE / 4
        private const val PARTIAL_MAX_DISTANCE = QUEUE_SIZE
        private const val PARTIAL_PATH_RANGE = 10
        val directions = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }
        val distances = Array(GRAPH_SIZE) { IntArray(GRAPH_SIZE) }

        private const val DIR_SOUTH = 0x1
        private const val DIR_WEST = 0x2
        private const val DIR_NORTH = 0x4
        private const val DIR_EAST = 0x8

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