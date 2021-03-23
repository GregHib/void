package world.gregs.voidps.engine.event

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
internal class EventHandlerBuilderTest {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>
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
        val action = mockk<TestEvent.(TestEvent) -> Unit>(relaxed = true)
        val builder = EventHandlerBuilder(filter)
        // When
        val handler = builder.build(action)
        // Then
        assertEquals(action, handler.action)
        assertEquals(filter, handler.filter)
    }
}