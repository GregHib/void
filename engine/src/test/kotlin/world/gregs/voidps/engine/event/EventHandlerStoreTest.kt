package world.gregs.voidps.engine.event

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

internal class EventHandlerStoreTest {

    @Test
    fun `Populated event handlers are in priority order`() {
        val events = Events(mockk())

        val entity: KClass<out EventDispatcher> = EventDispatcher::class
        val event: KClass<out Event> = Event::class
        val handler1 = EventHandler(event, { true }, Priority.HIGHISH, {})
        val handler2 = EventHandler(event, { true }, Priority.LOW, {})
        val handler3 = EventHandler(event, { true }, Priority.MEDIUM, {})
        val store = EventHandlerStore()
        store.add(entity, event, handler1)
        store.add(entity, event, handler2)
        store.add(entity, event, handler3)

        store.populate(entity, events)

        val ordered = events[Event::class]!!
        assertEquals(handler2, ordered[2])
        assertEquals(handler3, ordered[1])
        assertEquals(handler1, ordered[0])
    }

    @Test
    fun `Populated two handlers of same priority`() {
        val events = Events(mockk())

        val entity: KClass<out EventDispatcher> = EventDispatcher::class
        val event: KClass<out Event> = Event::class
        val handler1 = EventHandler(event, { true }, Priority.LOW, {})
        val handler2 = EventHandler(event, { true }, Priority.LOW, {})
        val store = EventHandlerStore()
        store.add(entity, event, handler1)
        store.add(entity, event, handler2)

        store.populate(entity, events)

        assertEquals(2, events[Event::class]!!.size)
    }
}