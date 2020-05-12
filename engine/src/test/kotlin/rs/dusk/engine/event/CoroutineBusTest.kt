package rs.dusk.engine.event

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import rs.dusk.engine.script.koin.KoinTestExtension

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */

class CoroutineBusTest : KoinTest {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>
    }

    val bus by inject<EventBus>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        printLogger()
        modules(eventBusModule)
    }

    @Test
    fun `Add first`() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.priority } returns 0
        val clazz = TestEvent::class
        // When
        bus.add(clazz, handler = handler)
        // Then
        assertEquals(handler, bus.get(clazz))
    }

    @Test
    fun `Add middle`() {
        // Given
        val second = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { second.next } returns null
        every { second.priority } returns 0
        val first = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { first.next } returns second
        every { first.priority } returns 2
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.priority } returns 1
        val clazz = TestEvent::class
        bus.add(clazz, handler = first)
        // When
        bus.add(clazz, handler = handler)
        // Then
        assertEquals(first, bus.get(clazz))
        verify { first.next = handler }
    }

    @Test
    fun `Add last`() {
        // Given
        val first = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { first.next } returns null
        every { first.priority } returns 10
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.priority } returns 0
        val clazz = TestEvent::class
        bus.add(clazz, handler = first)
        // When
        bus.add(clazz, handler = handler)
        // Then
        assertEquals(first, bus.get(clazz))
        verify { first.next = handler }
    }

    @Test
    fun emit() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.next } returns null
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        // When
        bus.emit(event)
        // Then
        coVerify { handler.actor.send(event) }
    }

    @Test
    fun `Emit cancelled`() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.next } returns null
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        event.cancel()
        // When
        bus.emit(event)
        // Then
        coVerify(exactly = 0) { handler.actor.send(event) }
    }
}