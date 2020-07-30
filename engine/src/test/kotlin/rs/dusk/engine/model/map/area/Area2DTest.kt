package rs.dusk.engine.model.map.area

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.model.map.Tile

internal class Area2DTest {

    @TestFactory
    fun `Area contains`() = arrayOf(
        Triple(12, 14, true),
        Triple(10, 10, true),
        Triple(14, 14, true),
        Triple(15, 14, false),
        Triple(10, 9, false)
    ).map { (x, y, expected) ->
        dynamicTest("Check if area contains $x $y") {
            // Given
            val tile = Tile(10, 10, 1)
            val area = Area2D(tile, 5, 5)
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
        val area = Area2D(tile, 3, 4)
        val iterator = area.iterator()
        val list = mutableListOf<Tile>()
        // When
        for (t in iterator) {
            list.add(t)
        }
        // Then
        val expected = mutableListOf<Tile>()
        for(x in 10 until 13) {
            for(y in 10 until 14) {
                expected.add(Tile(x, y, tile.plane))
            }
        }
        assertEquals(expected, list)
    }
}