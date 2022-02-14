package world.gregs.voidps.engine.action

import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.tick.Scheduler
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class SchedulerTest {

    private lateinit var scheduler: Scheduler

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
        scheduler = Scheduler(TestCoroutineDispatcher())
    }

    private fun tick(ticks: Int = 1) {
        repeat(ticks) {
            scheduler.run()
            GameLoop.tick++
        }
    }

    @Test
    fun `Await sync to schedule`() {
        var called = false
        scheduler.add {
            called = true
        }

        tick()

        assertTrue(called)
    }

    @Test
    fun `Fire with no tick delays`() {
        var called = false
        scheduler.add(ticks = 0) {
            called = true
        }
        tick()

        assertTrue(called)
    }

    @Test
    fun `Jobs fire in order of execution`() {
        var count = 0
        scheduler.add {
            assertEquals(0, count++)
        }
        scheduler.add {
            assertEquals(1, count++)
        }

        tick()
        assertEquals(2, count)
    }

    @Test
    fun `Cancelled jobs don't fire`() {
        var called = false
        val job = scheduler.add {
            called = true
        }
        job.cancel()
        tick()
        assertFalse(called)
    }

    @Test
    fun `Looped calls`() {
        var calls = 0
        scheduler.add(loop = 1) {
            tick++
            calls++
        }
        tick(4)
        assertEquals(4, calls)
    }

    @Test
    fun `Modified job stays in order`() {
        var called = false
        scheduler.add(1) {
            tick += 5
        }
        scheduler.add(2) {
            called = true
        }
        tick(3)
        assertTrue(called)
    }

    @Test
    fun `Adding during tick also fires`() {
        var called = false
        scheduler.add(0) {
            scheduler.add(0) {
                called = true
            }
        }
        tick()
        assertTrue(called)
    }
}