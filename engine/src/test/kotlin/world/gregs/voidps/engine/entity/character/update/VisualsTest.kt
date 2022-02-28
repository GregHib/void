package world.gregs.voidps.engine.entity.character.update

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
internal class VisualsTest {

    lateinit var visuals: PlayerVisuals

    @BeforeEach
    fun setup() {
        visuals = PlayerVisuals(body = mockk())
    }

    @Test
    fun `Get visual aspect`() {
        // Given
        val visual = mockk<Visual>()
        visuals.aspects[1] = visual
        // When
        val result = visuals.getOrPut<Visual>(1) { mockk() }
        // Then
        assertEquals(visual, result)
    }

    @Test
    fun `Put visual aspect`() {
        // Given
        val visual = mockk<Visual>()
        // When
        val result = visuals.getOrPut(1) { visual }
        // Then
        assertEquals(visual, result)
    }

    @Test
    fun `Dirty flag`() {
        // Given
        val visual = mockk<Visual>()
        // When
        visuals.flag(0x100)
        // Then
        assertEquals(0x100, visuals.flag)
    }
}