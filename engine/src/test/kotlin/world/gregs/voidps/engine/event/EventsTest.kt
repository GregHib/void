package world.gregs.voidps.engine.event

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EventsTest {

    @Test
    fun `Handlers are called in priority order`() {
        val events = Events(mockk())

        val handler1: EventHandler = mockk(relaxed = true)
        val handler2: EventHandler = mockk(relaxed = true)
        val handler3: EventHandler = mockk(relaxed = true)
        every { handler1.priority } returns Priority.HIGHISH
        every { handler2.priority } returns Priority.LOW
        every { handler3.priority } returns Priority.MEDIUM
        events.addAll(Event::class, listOf(handler1, handler2, handler3))

        val ordered = events[Event::class]!!
        assertEquals(handler2, ordered[2])
        assertEquals(handler3, ordered[1])
        assertEquals(handler1, ordered[0])
    }
}