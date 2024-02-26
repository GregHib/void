package world.gregs.voidps.engine.event

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HandlersTest {

    private lateinit var handlers: Handlers
    private lateinit var dispatcher: EventDispatcher
    private lateinit var event: Event
    private lateinit var suspendableEvent: SuspendableEvent
    private lateinit var cancellableEvent: CancellableEvent

    @BeforeEach
    fun setup() {
        handlers = Handlers()
        dispatcher = object : EventDispatcher {}
        event = object : Event {
            override fun key(): String {
                return "key"
            }
        }
        suspendableEvent = object : SuspendableEvent {}
        cancellableEvent = object : CancellableEvent() {}
    }

    @Test
    fun `Send event to handler`() {
        var called = false
        handlers.add("key") {
            called = true
        }

        handlers.send(dispatcher, event)

        assertTrue(called)
        assertTrue(handlers.contains("key"))
    }

    @Test
    fun `Trying to override handler throws exception`() {
        handlers.add("key") {
        }
        assertThrows<IllegalArgumentException> {
            handlers.add("key") {
            }
        }
    }

    @Test
    fun `Send event to subscriber and cancel`() {
        var called = false
        handlers.subscribe("key") {
            if (this is CancellableEvent) {
                this.cancel()
            }
        }
        handlers.add("key") {
            called = true
        }

        handlers.send(dispatcher, cancellableEvent)

        assertFalse(called)
        assertTrue(handlers.contains("key"))
    }

    @Test
    fun `Send suspendable event`() {
        var called = false
        handlers.add("key") {
            called = true
        }

        handlers.send(dispatcher, suspendableEvent)

        assertTrue(called)
        assertTrue(handlers.contains("key"))
    }

    @Test
    fun `Send event to multiple subscribers`() {
        var calls = 0
        handlers.subscribe("key") {
            calls++
        }
        handlers.subscribe("key") {
            calls++
        }

        handlers.send(dispatcher, event)

        assertEquals(2, calls)
        assertTrue(handlers.contains("key"))
    }

    @Test
    fun `Clear removes all handlers and subscribers`() {
        var called = false
        handlers.add("key") {
            called = true
        }
        handlers.subscribe("key") {
            called = true
        }

        handlers.clear()
        handlers.send(dispatcher, suspendableEvent)

        assertFalse(called)
        assertFalse(handlers.contains("key"))
    }
}