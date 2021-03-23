package world.gregs.voidps.engine.event

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.inject
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.script.KoinMock
import kotlin.reflect.KClass

/**
 * @author GregHib <greg@gregs.world>
 * @since March 27, 2020
 */
@ExtendWith(MockKExtension::class)
internal class EventBusTest : KoinMock() {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>
    }

    override val modules = listOf(eventModule)

    @Test
    fun `Then action`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<TestEvent>(any(), any()) } just Runs
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        // When
        TestEvent then action
        // Then
        verify {
            bus.add<TestEvent>(any(), any())
            register(any<KClass<TestEvent>>(), any())
        }
    }

    @Test
    fun `Then filtered`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<TestEvent>(any(), any()) } just Runs
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        val filter: TestEvent.() -> Boolean = mockk(relaxed = true)
        // When
        TestEvent where filter then action
        // Then
        verify { bus.add<TestEvent>(any(), any()) }
    }

    @Test
    fun `Register handler`() {
        // Given
        val bus = declareMock<EventBus> {
            every { add<TestEvent>(any(), any()) } just Runs
        }
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        // When
        register(TestEvent::class, handler)
        // Then
        verify { bus.add(any(), handler) }
    }


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