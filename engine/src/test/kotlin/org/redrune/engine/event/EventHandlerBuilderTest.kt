package org.redrune.engine.event

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
internal class EventHandlerBuilderTest {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>()
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
        assertEquals(filter, result.filter)
    }

    @Test
    fun `Builder where sets builder filter`() {
        // Given
        val builder = mockk<EventHandlerBuilder<TestEvent>>(relaxed = true)
        val filter = mockk<TestEvent.() -> Boolean>(relaxed = true)
        // When
        builder where filter
        // Then
        verify { builder.filter = filter }
    }
}