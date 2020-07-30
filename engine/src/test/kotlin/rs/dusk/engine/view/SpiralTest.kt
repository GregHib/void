package rs.dusk.engine.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.client.update.task.viewport.Spiral

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class SpiralTest {

    @TestFactory
    fun `Spiral radius`() = arrayOf(
        1 to arrayOf(
            intArrayOf(2, 3, 4),
            intArrayOf(1, 0, 5),
            intArrayOf(8, 7, 6)
        ),
        2 to arrayOf(
            intArrayOf(12, 13, 14, 15, 16),
            intArrayOf(11, 2, 3, 4, 17),
            intArrayOf(10, 1, 0, 5, 18),
            intArrayOf(9, 8, 7, 6, 19),
            intArrayOf(24, 23, 22, 21, 20)
        )
    ).map {
        //Given
        val radius = it.first
        val expected = it.second
        //When
        dynamicTest("Spiral radius ${it.first}") {
            val spiral = Spiral.outwards(radius)
            print(spiral)
            // Then
            assertMap(expected, spiral)
        }
    }

    private fun assertMap(expected: Array<IntArray>, actual: Array<IntArray>) {
        val maxX = actual.maxBy { it[0] }!![0]
        val maxY = actual.maxBy { it[1] }!![1]

        for ((index, step) in actual.withIndex()) {
            val (x, y) = step
            assertEquals(expected[maxY - y][x + maxX], index)
        }
    }

    @Suppress("unused")
    fun print(steps: Array<IntArray>) {
        val maxX = steps.maxBy { it[0] }!![0]
        val minX = steps.minBy { it[0] }!![0]
        val maxY = steps.maxBy { it[1] }!![1]
        val minY = steps.minBy { it[1] }!![1]

        for (y in maxY downTo minY) {
            for (x in minX..maxX) {
                print("${steps.indexOfFirst { it[0] == x && it[1] == y }} ")
            }
            println()
        }

    }

}