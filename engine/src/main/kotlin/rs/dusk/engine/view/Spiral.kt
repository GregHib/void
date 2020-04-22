package rs.dusk.engine.view

import rs.dusk.engine.model.Chunk
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
object Spiral {

    val STEPS: Array<Array<IntArray>>

    init {
        STEPS = (0..15).map {
            outwards(it)
        }.toTypedArray()
    }

    inline fun spiral(tile: Tile, radius: Int, action: (Tile) -> Unit) {
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        for ((sx, sy) in STEPS[radius]) {
            val cx = x + sx
            val cy = y + sy
            action.invoke(Tile(cx, cy, plane))
        }
    }

    inline fun spiral(chunk: Chunk, radius: Int, action: (Chunk) -> Unit) {
        val x = chunk.x
        val y = chunk.y
        for ((sx, sy) in STEPS[radius]) {
            val cx = x + sx
            val cy = y + sy
            action.invoke(Chunk(cx, cy))
        }
    }

    /**
     * Creates array of delta coordinates of an outwards spiral from a set tile
     * @param radius The radius to spiral
     * @return array of all steps
     */
    fun outwards(radius: Int): Array<IntArray> {
        var x = 0
        var y = 0
        var direction = 1
        var steps = 1
        var count = 0
        // Loop until we've covered every point in the grid
        val area = (1 + radius * 2) * (1 + radius * 2)
        val array = arrayOfNulls<IntArray>(area)
        while (count < area) {
            // Repeats each step twice e.g.
            // 1, 1, 2, 2, 3, 3, 4, 4, 5, 5...
            repeat(2) {
                repeat(steps) {
                    // If in bounds
                    array[count++] = intArrayOf(x, y)
                    // Stop if reached max
                    if (count >= area) {
                        return array.requireNoNulls()
                    }
                    // Move one step in the direction
                    when (direction) {
                        0 -> y--
                        1 -> x--
                        2 -> y++
                        3 -> x++
                    }
                }
                // Turn direction
                direction = (direction + 1) % 4
            }
            steps++
        }
        return array.requireNoNulls()
    }
}