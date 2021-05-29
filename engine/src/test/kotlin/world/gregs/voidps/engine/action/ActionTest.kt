package world.gregs.voidps.engine.action

import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class ActionTest : KoinMock() {
    lateinit var scope: CoroutineScope
    lateinit var action: Action

    override val modules = listOf(eventModule)

    @BeforeEach
    fun setup() {
        scope = TestCoroutineScope()
        action = spyk(Action(mockk(relaxed = true), scope))
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
        val job = action.run(type, block)
        // Then
        assertNotNull(job)
        coVerify {
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
    fun `Delay awaits by number of ticks`() = runBlocking {
        // Given
        val ticks = 4
        mockkStatic("kotlinx.coroutines.flow.FlowKt")
        val flow: MutableStateFlow<Long> = mockk(relaxed = true)
        coEvery { flow.singleOrNull() } returns null
        GameLoop.setTestFlow(flow)
        // When
        action.delay(ticks)
        // Then
        assertEquals(Suspension.Tick, action.suspension)
        coVerify(exactly = ticks) {
            flow.singleOrNull()
        }
    }
}