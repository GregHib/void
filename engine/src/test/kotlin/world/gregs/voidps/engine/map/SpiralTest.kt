package world.gregs.voidps.engine.map

import org.junit.jupiter.api.Assertions.assertEquals
import world.gregs.voidps.type.Tile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.type.Delta

internal class SpiralTest {

    @TestFactory
    fun `Spiral radius`() = arrayOf(
        1 to arrayOf(
            intArrayOf(2, 3, 4),
            intArrayOf(1, 0, 5),
            intArrayOf(8, 7, 6),
        ),
        2 to arrayOf(
            intArrayOf(12, 13, 14, 15, 16),
            intArrayOf(11, 2, 3, 4, 17),
            intArrayOf(10, 1, 0, 5, 18),
            intArrayOf(9, 8, 7, 6, 19),
            intArrayOf(24, 23, 22, 21, 20),
        ),
    ).map {
        // Given
        val radius = it.first
        val expected = it.second
        // When
        dynamicTest("Spiral radius ${it.first}") {
            val spiral = Spiral.outwards(radius)
            print(spiral)
            // Then
            assertMap(expected, spiral)
        }
    }

    private fun assertMap(expected: Array<IntArray>, actual: Array<Delta>) {
        val maxX = actual.maxByOrNull { it.x }!!.x
        val maxY = actual.maxByOrNull { it.y }!!.y

        for ((index, delta) in actual.withIndex()) {
            assertEquals(expected[maxY - delta.y][delta.x + maxX], index)
        }
    }

    @Suppress("unused")
    fun print(steps: Array<Delta>) {
        val maxX = steps.maxByOrNull { it.x }!!.x
        val minX = steps.minByOrNull { it.x }!!.x
        val maxY = steps.maxByOrNull { it.y }!!.y
        val minY = steps.minByOrNull { it.y }!!.y

        for (y in maxY downTo minY) {
            for (x in minX..maxX) {
                print("${steps.indexOfFirst { it.x == x && it.y == y }} ")
            }
            println()
        }
    }

    @Test
    fun `A radius past the view's is built on demand`() {
        val tiles = Spiral.spiral(Tile(100, 100), 20).asSequence().toList()
        assertEquals(41 * 41, tiles.size)
        assertEquals(Tile(100, 100), tiles.first())
        assertTrue(tiles.all { it.x in 80..120 && it.y in 80..120 })
    }
}
