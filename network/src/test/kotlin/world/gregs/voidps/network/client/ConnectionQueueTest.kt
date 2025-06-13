package world.gregs.voidps.network.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConnectionQueueTest {

    private lateinit var queue: ConnectionQueue

    @BeforeEach
    fun setup() {
        queue = ConnectionQueue(2)
    }

    @Test
    fun `Disconnect on next run`() = runTest {
        var called = false
        queue.disconnect {
            called = true
        }

        queue.run()

        assertTrue(called)
    }

    @Test
    fun `Await for next run`() = runTest {
        var called = false
        val job = launch(Dispatchers.Unconfined) {
            queue.await()
            called = true
        }
        queue.run()
        assertTrue(called)
        assertTrue(job.isCompleted)
    }

    @Test
    fun `Awaits resumed are limited`() = runTest {
        val coroutines = (0 until 3).map {
            launch(Dispatchers.Unconfined) {
                queue.await()
            }
        }
        queue.run()
        assertEquals(2, coroutines.count { it.isCompleted })
        coroutines.forEach { it.cancelAndJoin() }
    }

    @Test
    fun `Disconnections execute before await`() = runTest {
        var calls = 0
        queue.disconnect {
            assertEquals(0, calls)
            calls++
        }
        val job = launch(Dispatchers.Unconfined) {
            queue.await()
            assertEquals(1, calls)
            calls++
        }

        queue.run()

        assertEquals(2, calls)
        assertTrue(job.isCompleted)
    }
}
