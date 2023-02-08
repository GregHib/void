package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class QueuedTimersTest {

    @Test
    fun `Cancelled jobs are removed`() {
        var processed = false
        val queue = QueuedTimers()
        queue.add {}
        val cancelled = queue.add {
            processed = true
        }
        cancelled.cancel()
        queue.run()
        assertFalse(processed)
    }

    @Test
    fun `Jobs past their tick are invoked`() {
        var processed = false
        val queue = QueuedTimers()
        queue.add(4) {
            processed = true
        }
        GameLoop.tick = 5
        queue.run()
        assertTrue(processed)
    }

    @Test
    fun `Jobs that loop are added back to the queue`() {
        var processed = false
        val queue = QueuedTimers()
        val job = queue.add(0, 2) {
            processed = true
        }
        GameLoop.tick = 6
        queue.run()
        assertEquals(8L, job.tick)
        assertTrue(processed)
    }

    @Test
    fun `Cancel all jobs on clear`() {
        var cancelled = false
        val queue = QueuedTimers()
        val job = queue.add(2, cancelExecution = true) {
            cancelled = true
        }
        queue.clear()
        assertNotEquals(2L, job.tick)
        assertTrue(cancelled)
    }
}