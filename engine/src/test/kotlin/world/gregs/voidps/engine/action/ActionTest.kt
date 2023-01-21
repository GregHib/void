package world.gregs.voidps.engine.action

import io.mockk.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.timer.Job
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
internal class ActionTest : KoinMock() {
    lateinit var action: Action
    lateinit var scheduler: Scheduler

    override val modules = listOf(module {
        single { EventHandlerStore() }
        single(createdAtStart = true) { Scheduler() }
    })

    @BeforeEach
    fun setup() {
        action = spyk(Action(mockk(relaxed = true), UnconfinedTestDispatcher()))
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
        // When
        action.cancel()
        // Then
        verify {
            continuation.cancel()
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
        coEvery { action.pause(0) } returns true
        // When
        runTest(UnconfinedTestDispatcher()) {
            action.run(type, block)
        }
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
        TestScope().launch(UnconfinedTestDispatcher()) {
            action.await<Unit>(value)
        }
        // Then
        verifyOrder {
            action.continuation = any()
            action.suspension = value
        }
    }

    @Disabled
    @Test
    fun `Delay awaits by number of ticks`() = runTest {
        // Given
        val ticks = 4
        // When
        action.pause(ticks)
        // Then
        assertEquals(Suspension.Tick, action.suspension)
        coVerify {
            scheduler.add(ticks, any<Int>(), any(), any())
        }
    }
}