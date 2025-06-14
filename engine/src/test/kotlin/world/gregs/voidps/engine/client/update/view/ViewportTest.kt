package world.gregs.voidps.engine.client.update.view

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class ViewportTest {

    lateinit var viewport: Viewport

    @BeforeEach
    fun setup() {
        viewport = spyk(Viewport())
    }

    @Test
    fun `Active player index`() {
        // Given
        val index = 24
        viewport.idlePlayers[index] = 2
        // When
        val result = viewport.isActive(index)
        // Then
        assertTrue(result)
        assertFalse(viewport.isIdle(index))
    }

    @Test
    fun `Idle player index`() {
        // Given
        val index = 7
        viewport.idlePlayers[index] = 3
        // When
        val result = viewport.isIdle(index)
        // Then
        assertTrue(result)
        assertFalse(viewport.isActive(index))
    }

    @TestFactory
    fun `Re-activate player indices with shift`() = arrayOf(
        5 to 2,
        4 to 2,
        3 to 1,
        2 to 1,
        1 to 0,
        0 to 0,
    ).map { (from, to) ->
        dynamicTest("Shift player index from $from to $to") {
            // Given
            val index = 365
            viewport.idlePlayers[index] = from
            // When
            viewport.shift()
            // Then
            assertEquals(to, viewport.idlePlayers[index])
        }
    }
}
