package rs.dusk.engine.event

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
internal class EventHandlerBuilderTest {

    private class TestEvent : Event<Unit>() {
        companion object : EventCompanion<TestEvent>
    }

    @Test
    fun `Priority sets builder priority`() {
        // Given
        val priority = 4
        // When
        val result = TestEvent priority priority
        // Then
        assertEquals(priority, result.priority)
    }

    @Test
    fun `Event where sets builder filter`() {
        // Given
        val filter = mockk<TestEvent.() -> Boolean>(relaxed = true)
        // When
        val result = TestEvent where filter
        // Then
        val built = result.build(mockk(relaxed = true))
        assertEquals(filter, built.filter)
    }

    @Test
    fun `Builder returns event handler with all values set`() {
        // Given
        val filter = mockk<TestEvent.() -> Boolean>(relaxed = true)
        val check = mockk<TestEvent.() -> Boolean>(relaxed = true)
        val priority = 4
        val action = mockk<TestEvent.(TestEvent) -> Unit>(relaxed = true)
        val builder = EventHandlerBuilder(filter, check, priority)
        // When
        val handler = builder.build(action)
        // Then
        assertEquals(action, handler.action)
        assertEquals(filter, handler.filter)
        assertEquals(check, handler.check)
        assertEquals(priority, handler.priority)
    }
}