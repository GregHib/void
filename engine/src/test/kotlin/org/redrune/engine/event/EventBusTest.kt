package org.redrune.engine.event

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since March 27, 2020
 */
@ExtendWith(MockKExtension::class)
internal class EventBusTest : KoinTest {

    private class TestEvent : Event() {
        companion object : EventCompanion<TestEvent>()
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(eventBusModule)
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    @Test
    fun then() {
        // Given
        val bus = declareMock<EventBus> {
            every { addLast<TestEvent>(any(), any()) } answers {}
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        // When
        TestEvent then action
        // Then
        verify {
            bus.addLast<TestEvent>(any(), any())
//            register(any<KClass<EventCompanion<TestEvent>>>(), any()).hint(Unit::class) FIXME
        }
    }

    @Test
    fun `Then filtered`() {
        // Given
        val bus = declareMock<EventBus> {
            every { addLast<TestEvent>(any(), any()) } answers {}
        }
        val action: TestEvent.(TestEvent) -> Unit = mockk(relaxed = true)
        val filter: TestEvent.() -> Boolean = mockk(relaxed = true)
        // When
        TestEvent where filter then action
        // Then
        verify { bus.addLast<TestEvent>(any(), any()) }
    }

    @Test
    fun `Register handler`() {
        // Given
        val bus = declareMock<EventBus> {
            every { addLast<TestEvent>(any(), any()) } answers {}
        }
        val handler = mockk<EventHandler<TestEvent>>(relaxed = true)
        // When
        register(TestEvent.Companion::class, handler)
        // Then
        verify { bus.addLast(any(), handler) }
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