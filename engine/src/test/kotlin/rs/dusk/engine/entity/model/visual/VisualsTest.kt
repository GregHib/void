package rs.dusk.engine.entity.model.visual

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 26, 2020
 */
internal class VisualsTest {

    @Test
    fun `Add appends to flag`() {
        // Given
        val visuals = Visuals()
        val visual = mockk<Visual>()
        // When
        visuals.add(0x100, visual)
        // Then
        assertEquals(0x100, visuals.flag)
        assert(visuals.aspects.containsValue(visual))
    }

    @Test
    fun `Clear removes all`() {
        // Given
        val visuals = Visuals()
        val visual = mockk<Visual>()
        visuals.flag = 0x200
        visuals.aspects[visual::class] = visual
        // When
        visuals.clear()
        // Then
        assertEquals(0, visuals.flag)
        assert(visuals.aspects.isEmpty())
    }
}