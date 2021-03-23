package world.gregs.voidps.engine.event

import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.inject
import world.gregs.voidps.engine.script.KoinMock

/**
 * @author GregHib <greg@gregs.world>
 * @since March 27, 2020
 */
@ExtendWith(MockKExtension::class)
internal class EventBusTest : KoinMock() {

    private class TestEvent : Event {
        companion object : EventCompanion<TestEvent>
    }

    override val modules = listOf(eventModule)

    val bus by inject<EventBus>()

    @Test
    fun `Add first`() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        val clazz = TestEvent::class
        // When
        bus.add(clazz, handler = handler)
        // Then
        assertEquals(handler, bus.get(clazz))
    }

    @Test
    fun `Emit filtered by applies`() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.applies(any()) } returns false
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        // When
        bus.emit(event)
        // Then
        coVerify(exactly = 0) {
            handler.invoke(event)
        }
    }

    @Test
    fun emit() {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.applies(any()) } returns true
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        // When
        bus.emit(event)
        // Then
        coVerify {
            handler.applies(event)
            handler.applies(event)
            handler.invoke(event)
        }
    }
}