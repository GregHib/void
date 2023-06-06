package world.gregs.voidps.engine.event

import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EventsTest {

    @Test
    fun `Cancelled events stops processing`() {
        val events = Events(mockk())
        val event = object : CancellableEvent() {}
        val klass = event::class
        var cancelled = false
        val block: CancellableEvent.(EventDispatcher) -> Unit = {
            cancelled = true
            cancel()
        }
        val handler1 = EventHandler(klass, { true }, Priority.HIGH, block as Event.(EventDispatcher) -> Unit)
        var called = false
        val handler2 = EventHandler(klass, { true }, Priority.MEDIUM, { called = true })
        events.set(mapOf(klass to listOf(handler1, handler2)))

        assertTrue(events.emit(event))

        assertTrue(cancelled)
        assertFalse(called)
    }

    @Test
    fun `Handlers with conditions not met aren't called`() {
        val events = Events(mockk())
        val event = object : Event {}
        val klass = event::class
        var called1 = false
        var called2 = false
        val handler1 = EventHandler(klass, { false }, Priority.HIGH, { called1 = true })
        val handler2 = EventHandler(klass, { true }, Priority.MEDIUM, { called2 = true })
        events.set(mapOf(klass to listOf(handler1, handler2)))

        assertTrue(events.emit(event))

        assertFalse(called1)
        assertTrue(called2)
    }

    @Test
    fun `Emit returns false if nothing executed`() {
        val events = Events(mockk())
        val event = object : Event {}
        val klass = event::class
        val handler = EventHandler(klass, { false }, Priority.HIGH, {})
        events.set(mapOf(klass to listOf(handler)))

        assertFalse(events.emit(event))
    }
}