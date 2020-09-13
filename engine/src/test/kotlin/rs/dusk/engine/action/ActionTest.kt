package rs.dusk.engine.action

import io.mockk.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.mock.declareMock
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.engine.task.DelayTask
import rs.dusk.engine.task.Task
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.executorModule
import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class ActionTest : KoinMock() {
    lateinit var action: Action
    lateinit var executor: TaskExecutor

    override val modules = listOf(eventModule, executorModule)

    @BeforeEach
    fun setup() {
        action = spyk(Action())
        executor = declareMock {
            every { execute(any()) } answers {
                val task: Task = arg(0)
                task.run(0)
                task.cancel()
            }
            every { tick } returns 0
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
        val value = Throwable()
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
        verify {
            action.cancel()
        }
    }

    @Test
    fun `Cancel with unexpected type`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        action.type = ActionType.Movement
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
        val type = ActionType.Movement
        val coroutine: Continuation<Unit> = mockk(relaxed = true)
        mockkStatic("kotlin.coroutines.ContinuationKt")
        action.continuation = continuation
        every { action.cancel(any()) } just Runs
        coEvery { action.delay(0) } returns true
        every { block.createCoroutine(action, ActionContinuation) } returns coroutine
        // When
        action.run(type, block)
        // Then
        coVerifyOrder {
            action.cancel(type)
            block.createCoroutine(action, ActionContinuation)
            action.delay(0)
        }
    }

    @Test
    fun `Await sets continuation and suspension`() {
        // Given
        val continuation: CancellableContinuation<Unit> = mockk(relaxed = true)
        action.continuation = continuation
        val value = Suspension.Tick
        // When
        GlobalScope.launch {
            action.await<Unit>(value)
        }
        // Then
        verifyOrder {
            action.continuation = any()
            action.suspension = value
        }
    }

    @Test
    fun `Delay awaits by number of ticks`() = runBlocking {
        // Given
        val value = 4L
        every { executor.execute(any()) } answers {
            val task: DelayTask = arg(0)
            assertEquals(value, task.executionTick)
            task.run(value)
        }
        // When
        action.delay(value.toInt())
        // Then
        verify {
            executor.execute(any<DelayTask>())
        }
    }
}