package world.gregs.voidps.engine.action

import io.mockk.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.tick.Job
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.tick.schedulerModule
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class ActionTest : KoinMock() {
    lateinit var scope: CoroutineScope
    lateinit var action: Action
    lateinit var scheduler: Scheduler

    override val modules = listOf(eventModule, schedulerModule)

    @BeforeEach
    fun setup() {
        scope = TestCoroutineScope()
        action = spyk(Action(mockk(relaxed = true), scope))
        scheduler = declareMock {
            every { add(any(), any<Int>(), any(), any()) } answers {
                val block: Job.(Long) -> Unit = arg(3)
                val job = Job(0, -1, false, block)
                block.invoke(job, 0)
                job
            }
        }
    }

    @Test
    fun `Not suspended with just continuation`() {
        // Given
        action.continuation = mockk(relaxed = true)
        // When
        val result = action.isSuspended()
        // Then
        assertFalse(result)
    }

    @Test
    fun `Not suspended with just suspension type`() {
        // Given
        action.suspension = mockk(relaxed = true)
        // When
        val result = action.isSuspended()
        // Then
        assertFalse(result)
    }

    @Test
    fun `Suspended with continuation and suspension type`() {
        // Given
        action.suspension = mockk(relaxed = true)
        action.continuation = mockk(relaxed = true)
        // When
        val result = action.isSuspended()
        // Then
        assertTrue(result)
    }

    @Test
    fun `Not suspended with either`() {
        // Given
        action.suspension = null
        action.continuation = null
        // When
        val result = action.isSuspended()
        // Then
        assertFalse(result)
    }

    @Test
    fun `Resume sets null`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.suspension = mockk(relaxed = true)
        action.continuation = continuation
        val value = Unit
        clearMocks(action)
        // When
        action.resume(value)
        // Then
        verify {
            continuation.resume(value)
        }
        assertNull(action.continuation)
        assertNull(action.suspension)
    }

    @Test
    fun `Resume ignored if not suspended`() {
        // Given
        action.continuation = null
        val value = Unit
        clearAllMocks()
        // When
        action.resume(value)
        // Then
        verify(exactly = 0) {
            action.continuation = null
            action.suspension = null
        }
    }

    @Test
    fun `Cancel resumes with exception`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        val value = CancellationException()
        // When
        action.cancel(value)
        // Then
        verify {
            continuation.resumeWithException(value)
        }
    }

    @Test
    fun `Cancel with expected type`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        action.type = ActionType.Teleport
        // When
        action.cancel(ActionType.Teleport)
        // Then
        assertNull(action.continuation)
    }

    @Test
    fun `Cancel with unexpected type`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        action.type = ActionType.Follow
        // When
        action.cancel(ActionType.Teleport)
        // Then
        verify(exactly = 0) {
            action.cancel()
        }
    }

    @Test
    fun `Run creates and starts coroutine`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        val block: suspend Action.() -> Unit = mockk(relaxed = true)
        val type = ActionType.Follow
        action.continuation = continuation
        every { action.cancel(any()) } just Runs
        coEvery { action.delay(0) } returns true
        // When
        action.run(type, true, block)
        // Then
        coVerify {
            scheduler.add(any(), any<Int>(), any(), any())
            action.cancelAndJoin(any())
        }
    }

    @Test
    fun `Await sets continuation and suspension`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        val value = Suspension.Tick
        // When
        scope.launch {
            action.await<Unit>(value)
        }
        // Then
        verifyOrder {
            action.continuation = any()
            action.suspension = value
        }
    }

    @Test
    fun `Delay awaits by number of ticks`() = runBlockingTest {
        // Given
        val ticks = 4
        // When
        action.delay(ticks)
        // Then
        assertEquals(Suspension.Tick, action.suspension)
        coVerify {
            scheduler.add(ticks, any<Int>(), any(), any())
        }
    }
}