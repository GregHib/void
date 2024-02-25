package world.gregs.voidps.engine.event

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EventStoreTest {

    private lateinit var store: EventStore

    @BeforeEach
    fun setup() {
        store = EventStore()
    }

    private class TestEvent : Event
    private class TestSuspendableEvent : SuspendableEvent

    @Test
    fun `Event is emitted to handler`() {
        var called = false
        val handler = EventHandler(block = {
            called = true
        })
        store.add(World::class, TestEvent::class, handler)
        store.init()

        val emitted = store.emit(World, TestEvent())

        assertTrue(emitted)
        assertTrue(called)
    }

    @Test
    fun `Suspendable event is emitted to handler`() {
        var called = false
        val handler = EventHandler(block = {
            called = true
        })
        store.add(World::class, TestSuspendableEvent::class, handler)
        store.init()

        val event = TestSuspendableEvent()
        val emitted = store.emit(World, event)

        assertTrue(emitted)
        assertTrue(called)
        assertTrue(store.contains(World, event))
    }

    @Test
    fun `Handlers with different priorities are sorted on init`() {
        var called = 0
        store.add(World::class, TestEvent::class, EventHandler(priority = Priority.HIGH, block = {
            assertEquals(0, called)
            called++
        }))
        store.add(World::class, TestEvent::class, EventHandler(priority = Priority.LOW, block = {
            assertEquals(1, called)
            called++
        }))
        store.init()

        val emitted = store.emit(World, TestEvent())

        assertTrue(emitted)
        assertEquals(2, called)
    }

    @Test
    fun `Handler with parents are emitted to all`() {
        var called = false
        val handler = EventHandler(block = {
            called = true
        })
        store.add(Player::class, TestEvent::class, handler)
        store.init()

        val character: Character = Player()
        val emitted = store.emit(character, TestEvent())

        assertTrue(emitted)
        assertTrue(called)
    }

    @Test
    fun `Clear removes all handlers`() {
        var called = false
        val handler = EventHandler(block = {
            called = true
        })
        store.add(World::class, TestSuspendableEvent::class, handler)
        store.clear()

        val event = TestSuspendableEvent()
        val emitted = store.emit(World, event)

        assertFalse(emitted)
        assertFalse(called)
        assertFalse(store.contains(World, event))
    }
}