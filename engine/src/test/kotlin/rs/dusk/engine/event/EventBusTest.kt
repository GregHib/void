package rs.dusk.engine.event

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.inject
import org.koin.test.mock.declareMock
import rs.dusk.engine.script.KoinMock
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 27, 2020
 */
@ExtendWith(MockKExtension::class)
internal class EventBusTest : KoinMock() {

    private class TestEvent : Event<Int>() {
        companion object : EventCompanion<TestEvent>
    }

    override val modules = listOf(eventModule)

    @Test
    fun `Then action`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<Int, TestEvent>(any(), any()) } just Runs
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        // When
        TestEvent then action
        // Then
        verify {
            bus.add<Int, TestEvent>(any(), any())
            register(any<KClass<TestEvent>>(), any())
        }
    }

    @Test
    fun `Then filtered`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<Int, TestEvent>(any(), any()) } just Runs
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        val filter: TestEvent.() -> Boolean = mockk(relaxed = true)
        // When
        TestEvent where filter then action
        // Then
        verify { bus.add<Int, TestEvent>(any(), any()) }
    }

    @Test
    fun `Register handler`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<Int, TestEvent>(any(), any()) } just Runs
        }
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        // When
        register(TestEvent::class, handler)
        // Then
        verify { bus.add(any(), handler) }
    }


    val bus by inject<EventBus>()

    @Test
    fun `Add first`() {
        // Given
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.priority } returns 0
        val clazz = TestEvent::class
        // When
        bus.add(clazz, handler = handler)
        // Then
        Assertions.assertEquals(handler, bus.get(clazz))
    }

    @Test
    fun `Add middle`() {
        // Given
        val second = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { second.next } returns null
        every { second.priority } returns 0
        val first = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { first.next } returns second
        every { first.priority } returns 2
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.priority } returns 1
        val clazz = TestEvent::class
        bus.add(clazz, handler = first)
        // When
        bus.add(clazz, handler = handler)
        // Then
        Assertions.assertEquals(first, bus.get(clazz))
        verify { first.next = handler }
    }

    @Test
    fun `Add last`() {
        // Given
        val first = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { first.next } returns null
        every { first.priority } returns 10
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.priority } returns 0
        val clazz = TestEvent::class
        bus.add(clazz, handler = first)
        // When
        bus.add(clazz, handler = handler)
        // Then
        Assertions.assertEquals(first, bus.get(clazz))
        verify { first.next = handler }
    }

    @Test
    fun `Emit failing pre-check`() {
        // Given
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.applies(any()) } returns true
        every { handler.checked(any()) } returns false
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
    fun `Emit filtered by applies`() {
        // Given
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.applies(any()) } returns false
        every { handler.checked(any()) } returns true
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
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.applies(any()) } returns true
        every { handler.checked(any()) } returns true
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        event.result = 42
        // When
        val result = bus.emit(event)
        // Then
        assertEquals(42, result)
        coVerify {
            handler.applies(event)
            handler.checked(event)
            handler.applies(event)
            handler.invoke(event)
        }
    }

    @Test
    fun `Emit event cancelled`() {
        // Given
        val handler = mockk<EventHandler<Int, TestEvent>>(relaxed = true)
        every { handler.next } returns null
        every { handler.checked(any()) } returns true
        val clazz = TestEvent::class
        bus.add(clazz, handler = handler)
        val event = TestEvent()
        event.cancel()
        // When
        val result = bus.emit(event)
        // Then
        assertNull(result)
        coVerify(exactly = 0) {
            handler.invoke(event)
        }
    }
}