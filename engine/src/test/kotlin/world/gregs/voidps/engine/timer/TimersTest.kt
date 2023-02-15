package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

abstract class TimersTest {

    lateinit var timers: Timers

    @Test
    fun `Timers repeat at a consistent interval`() {
        var count = 0L
        timers.add("timer", 2) { counter ->
            assertEquals(count++, counter)
        }
        repeat(7) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals(3, count)
    }

    @Test
    fun `Timers with 0 delay repeats once a run`() {
        var count = 0L
        timers.add("", 0) { counter ->
            assertEquals(count++, counter)
        }
        repeat(2) {
            timers.run()
            GameLoop.tick++
        }
        assertEquals(2, count)
    }

    @Test
    fun `Cancelled timers are removed`() {
        timers.add("", 1) {
            cancel()
        }
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains(""))
    }

    @Test
    fun `Clearing a timer cancels it`() {
        timers.add("timer", 5) {}
        timers.clear("timer")
        assertFalse(timers.contains("timer"))
    }

    @Test
    fun `Cleared timers are cancelled`() {
        val timer = timers.add("", 5) {}
        timers.clearAll()
        assertFalse(timers.contains(""))
    }
}