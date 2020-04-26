package rs.dusk.engine.entity.model.visual

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
@Suppress("UNCHECKED_CAST")
internal class VisualsTest {

    @Test
    fun `Get visual aspect`() {
        // Given
        val visuals = Visuals()
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
        val visuals = Visuals()
        val visual = mockk<Visual>()
        // When
        val result = visuals.getOrPut(1) { visual }
        // Then
        assertEquals(visual, result)
    }

    @Test
    fun `Dirty flag`() {
        // Given
        val visuals = Visuals()
        val visual = mockk<Visual>()
        // When
        visuals.flag(0x100)
        // Then
        assertEquals(0x100, visuals.flag)
    }

    @Test
    fun `Clear removes all`() {
        // Given
        val visuals = Visuals()
        val visual = mockk<Visual>()
        visuals.flag = 0x200
        visuals.aspects[0x200] = visual
        // When
        visuals.clear()
        // Then
        assertEquals(0, visuals.flag)
        assert(visuals.aspects.isEmpty())
    }
}