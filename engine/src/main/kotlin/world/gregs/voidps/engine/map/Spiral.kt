package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

object Spiral {

    private val STEPS: Array<Array<Delta>>

    init {
        STEPS = (0..VIEW_RADIUS).map(::outwards).toTypedArray()
    }

    fun spiral(tile: Tile, radius: Int): Iterator<Tile> = TileIterator(tile, STEPS[radius])

    internal class TileIterator(
        private val tile: Tile,
        private val steps: Array<Delta>,
    ) : Iterator<Tile> {

        private var index = 0

        override fun hasNext(): Boolean = index < steps.size
        override fun next(): Tile = tile.add(steps[index++])
    }

    fun spiral(zone: Zone, radius: Int): Iterator<Zone> = ZoneIterator(zone, STEPS[radius])

    internal class ZoneIterator(
        private val zone: Zone,
        private val steps: Array<Delta>,
    ) : Iterator<Zone> {
        private var index = 0

        override fun hasNext(): Boolean = index < steps.size

        override fun next(): Zone = zone.add(steps[index++])
    }

    /**
     * Creates array of delta coordinates of an outwards spiral from a set tile
     * @param radius The radius to spiral
     * @return array of all steps
     */
    fun outwards(radius: Int): Array<Delta> {
        var x = 0
        var y = 0
        var direction = 1
        var steps = 1
        var count = 0
        // Loop until we've covered every point in the grid
        val area = (1 + radius * 2) * (1 + radius * 2)
        val array = arrayOfNulls<Delta>(area)
        while (count < area) {
            // Repeats each step twice e.g.
            // 1, 1, 2, 2, 3, 3, 4, 4, 5, 5...
            repeat(2) {
                repeat(steps) {
                    // If in bounds
                    array[count++] = Delta(x, y)
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

fun Tile.spiral(radius: Int) = Spiral.spiral(this, radius)

fun Zone.spiral(radius: Int) = Spiral.spiral(this, radius)
