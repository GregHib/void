package rs.dusk.engine.action

import io.mockk.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.engine.TickAction
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get
import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

internal class SchedulerTest : KoinMock() {
    lateinit var actions: Scheduler

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        actions = spyk(Scheduler())
    }

    @Test
    fun `Action creates and starts coroutine`() {
        // Given
        val block: suspend CoroutineScope.() -> Unit = mockk(relaxed = true)
        val coroutine: Continuation<Unit> = mockk(relaxed = true)
        mockkStatic("kotlin.coroutines.ContinuationKt")
        every { block.createCoroutine(actions, ActionContinuation) } returns coroutine
        // When
        actions.add(block)
        // Then
        coVerifyOrder {
            block.invoke(any())
        }
    }

    @Test
    fun `Tick resumes active coroutine`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        val bus: EventBus = get()
        mockkStatic("kotlin.coroutines.ContinuationKt")
        actions.active.add(continuation)
        every { continuation.isActive } returns true
        // When
        bus.emit(TickAction)
        // Then
        verify {
            continuation.resume(Unit)
        }
        assertEquals(1, actions.active.size)
    }

    @Test
    fun `Inactive coroutines are removed from queue`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        val bus: EventBus = get()
        mockkStatic("kotlin.coroutines.ContinuationKt")
        actions.active.add(continuation)
        every { continuation.isActive } returns false
        // When
        bus.emit(TickAction)
        // Then
        verify {
            continuation.resume(Unit)
        }
        assertEquals(0, actions.active.size)
    }

    @Test
    fun `Cancelled coroutines are removed from queue`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        val bus: EventBus = get()
        mockkStatic("kotlin.coroutines.ContinuationKt")
        actions.active.add(continuation)
        every { continuation.isCancelled } returns false
        // When
        bus.emit(TickAction)
        // Then
        verify {
            continuation.resume(Unit)
        }
        assertEquals(0, actions.active.size)
    }

    @Test
    fun `Completed coroutines are removed from queue`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        val bus: EventBus = get()
        mockkStatic("kotlin.coroutines.ContinuationKt")
        actions.active.add(continuation)
        every { continuation.isCompleted } returns false
        // When
        bus.emit(TickAction)
        // Then
        verify {
            continuation.resume(Unit)
        }
        assertEquals(0, actions.active.size)
    }

    @Test
    fun `Delay awaits by number of ticks`() = runBlocking {
        // Given
        val ticks = 2
        every { actions.active.add(any()) } coAnswers {
            arg<CancellableContinuation<Unit>>(0).resume(Unit)
            true
        }
        // When
        actions.delay(ticks)
        // Then
        verify(exactly = ticks) {
            actions.active.add(any())
        }
    }
}