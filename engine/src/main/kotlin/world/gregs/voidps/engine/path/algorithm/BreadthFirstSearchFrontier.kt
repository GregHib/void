package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile

/**
 * All the grid tiles visited or to be visited by the [BreadthFirstSearch] algorithm
 */
class BreadthFirstSearchFrontier(val mapSize: Int = 128) {

    private val visits: IntArray = IntArray(mapSize * mapSize)
    private var visit = 0
    private val queue: IntArray = IntArray(mapSize * mapSize)
    private var writeIndex = 0
    private var readIndex = 0
    private var start = Tile.EMPTY

    fun start(tile: Tile) {
        reset()
        start = tile.minus(mapSize / 2, mapSize / 2)
        visit(tile, 0, 0)
    }

    private fun reset() {
        visit = visit + 1 and 0x3fff
        writeIndex = 0
        readIndex = 0
    }

    fun queue(tile: Tile) {
        queue[writeIndex++] = tile.id
    }

    fun isNotEmpty() = readIndex < writeIndex

    fun poll(): Tile = Tile(queue[readIndex++])

    fun visit(tile: Tile, cost: Int, dir: Int) {
        if (outOfBounds(tile.x, tile.y)) {
            return
        }
        queue(tile)
        visits[index(tile.x, tile.y)] = pack(cost, visit, dir)
    }

    fun visited(tile: Tile, default: Boolean): Boolean {
        if (outOfBounds(tile.x, tile.y)) {
            return false
        }
        val value = visits.getOrNull(index(tile.x, tile.y)) ?: return default
        return getVisit(value) == visit
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

    companion object {
        private fun getDir(value: Int) = value shr 28

        private fun getCost(value: Int) = value shr 14 and 0x3fff

        private fun getVisit(value: Int) = value and 0x3fff

        private fun pack(cost: Int, visit: Int, dir: Int) = visit or (cost shl 14) or (dir shl 28)
    }
}