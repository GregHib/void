package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerTest {

    @Test
    fun `Overriding cancels job`() {
        val timer = Timer()
        val job = timer.add {  }
        timer.add {}
        assertTrue(job.cancelled)
    }

    @Test
    fun `Cancelled job is removed`() {
        var processed = false
        val timer = Timer()
        val cancelled = timer.add {
            processed = true
        }
        cancelled.cancel()
        timer.run()
        assertFalse(processed)
    }

    @Test
    fun `Jobs past their tick are invoked`() {
        var processed = false
        val timer = Timer()
        timer.add(4) {
            processed = true
        }
        GameLoop.tick = 5
        timer.run()
        assertTrue(processed)
    }

    @Test
    fun `Jobs that loop are added back to the queue`() {
        var processed = false
        val timer = Timer()
        val job = timer.add(0, 2) {
            processed = true
        }
        GameLoop.tick = 6
        timer.run()
        assertEquals(8L, job.tick)
        assertTrue(processed)
    }

    @Test
    fun `Cancel job on clear`() {
        var cancelled = false
        val timer = Timer()
        val job = timer.add(2, cancelExecution = true) {
            cancelled = true
        }
        timer.clear()
        assertNotEquals(2L, job.tick)
        assertTrue(cancelled)
    }
}