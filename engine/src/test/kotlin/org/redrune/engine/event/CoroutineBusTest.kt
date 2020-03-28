package org.redrune.engine.event

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.redrune.engine.script.koin.KoinTestExtension

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */

class CoroutineBusTest : KoinTest {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>()
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
        val clazz = TestEvent::class
        // When
        bus.addFirst(clazz, handler = handler)
        // Then
        assertEquals(handler, bus.get(clazz))
    }

    @Test
    fun `Add last`() {
        // Given
        val first = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { first.next } returns null
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        val clazz = TestEvent::class
        bus.addFirst(clazz, handler = first)
        // When
        bus.addLast(clazz, handler = handler)
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
        bus.addFirst(clazz, handler = handler)
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
        bus.addFirst(clazz, handler = handler)
        val event = TestEvent()
        event.cancel()
        // When
        bus.emit(event)
        // Then
        coVerify(exactly = 0) { handler.actor.send(event) }
    }
}