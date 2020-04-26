package rs.dusk.engine.entity.model.visual

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

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
        val clazz = visual::class as KClass<Visual>
        visuals.aspects[visual::class] = visual
        // When
        val result = visuals.getOrPut(clazz) { mockk() }
        // Then
        assertEquals(visual, result)
    }

    @Test
    fun `Put visual aspect`() {
        // Given
        val visuals = Visuals()
        val visual = mockk<Visual>()
        val clazz = visual::class as KClass<Visual>
        // When
        val result = visuals.getOrPut(clazz) { visual }
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
        visuals.aspects[visual::class] = visual
        // When
        visuals.clear()
        // Then
        assertEquals(0, visuals.flag)
        assert(visuals.aspects.isEmpty())
    }
}