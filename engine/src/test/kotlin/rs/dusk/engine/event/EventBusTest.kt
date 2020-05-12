package rs.dusk.engine.event

import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.mock.declareMock
import rs.dusk.engine.script.KoinMock
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 27, 2020
 */
@ExtendWith(MockKExtension::class)
internal class EventBusTest : KoinMock() {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>
    }

    override val modules = listOf(eventBusModule)

    @Test
    fun then() {
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

    @Test
    fun `Set actor`() = runBlocking {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        // When
        setActor(handler, action, null)
        delay(100)
        // Then
        verify { handler.actor = any() }
    }

    @Test
    fun `Set actor filter`() = runBlocking {
        // Given
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        val filter: TestEvent.() -> Boolean = mockk(relaxed = true)
        // When
        setActor(handler, action, filter)
        delay(100)
        // Then
        verify { handler.actor = any() }
    }
}