package rs.dusk.engine.model.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class AreaTest {

    @TestFactory
    fun `Area contains`() = arrayOf(
        Triple(12, 14, true),
        Triple(10, 10, true),
        Triple(15, 15, true),
        Triple(16, 15, false),
        Triple(10, 9, false)
    ).map { (x, y, expected) ->
        dynamicTest("Check if area contains $x $y") {
            // Given
            val tile = Tile(10, 10, 1)
            val area = Area(tile, 5, 5)
            // When
            val result = area.contains(x, y)
            // Then
            assertEquals(expected, result)
        }
    }

    @Test
    fun `Iterate values`() {
        // Given
        val tile = Tile(10, 10, 1)
        val area = Area(tile, 3, 4)
        val iterator = area.iterator()
        val list = mutableListOf<Tile>()
        // When
        for (t in iterator) {
            list.add(t)
        }
        // Then
        val expected = listOf(
            Tile(10, 10, 1),
            Tile(10, 11, 1),
            Tile(10, 12, 1),
            Tile(11, 10, 1),
            Tile(11, 11, 1),
            Tile(11, 12, 1),
            Tile(12, 10, 1),
            Tile(12, 11, 1),
            Tile(12, 12, 1),
            Tile(13, 10, 1),
            Tile(13, 11, 1),
            Tile(13, 12, 1)
        )
        assertEquals(expected, list)
    }
}