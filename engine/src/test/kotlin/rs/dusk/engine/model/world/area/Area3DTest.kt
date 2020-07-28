package rs.dusk.engine.model.world.area

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.model.world.Tile

internal class Area3DTest {

    private data class Scenario(val x: Int, val y: Int, val plane: Int, val outcome: Boolean)

    @TestFactory
    fun `Area contains`() = arrayOf(
        Scenario(12, 14, 1, true),
        Scenario(10, 10, 1, true),
        Scenario(14, 14, 1, true),
        Scenario(15, 14, 1, false),
        Scenario(10, 9, 1, false),
        Scenario(11, 11, 0, false),
        Scenario(11, 11, 1, true),
        Scenario(11, 11, 2, false)
    ).map { (x, y, plane, expected) ->
        dynamicTest("Check if area contains $x $y") {
            // Given
            val tile = Tile(10, 10, 1)
            val area = Area3D(tile, 5, 5, 1)
            // When
            val result = area.contains(x, y, plane)
            // Then
            assertEquals(expected, result)
        }
    }

    @Test
    fun `Iterate values`() {
        // Given
        val tile = Tile(10, 10, 1)
        val area = Area3D(tile, 3, 4, 1)
        val iterator = area.iterator()
        val list = mutableListOf<Tile>()
        // When
        for (t in iterator) {
            list.add(t)
        }
        // Then
        val expected = mutableListOf<Tile>()
        for(z in 1 until 2) {
            for(x in 10 until 13) {
                for(y in 10 until 14) {
                    expected.add(Tile(x, y, z))
                }
            }
        }
        assertEquals(expected, list)
    }
}