package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import java.util.*

abstract class TimersTest {

    lateinit var emitted: LinkedList<Pair<String, Boolean>>
    lateinit var timers: Timers

    var startInterval: Int = 0
    var tickInterval: Int = 0

    open fun setup() {
        GameLoop.tick = 0
        emitted = LinkedList()
    }

    @Test
    fun `Restart a timer`() {
        startInterval = 2
        timers.restart("timer")
        assertTrue(timers.contains("timer"))
        assertEquals("start_timer" to true, emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Cancelled start event doesn't add timer`() {
        startInterval = Timer.CANCEL
        assertFalse(timers.start("timer"))
        assertFalse(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers emit at a constant interval`() {
        startInterval = 2
        tickInterval = Timer.CONTINUE
        assertTrue(timers.start("timer"))
        repeat(5) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        repeat(2) {
            assertEquals("tick_timer" to false, emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timer can temp modify interval`() {
        startInterval = 2
        tickInterval = 1
        assertTrue(timers.start("timer"))
        repeat(4) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        repeat(2) {
            // ).apply { nextInterval = 1 }
            assertEquals("tick_timer" to false, emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers with 0 delay repeats every tick`() {
        startInterval = 0
        timers.start("timer")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        repeat(3) {
            assertEquals("tick_timer" to false, emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Timers with cancelled tick events are removed`() {
        startInterval = 0
        tickInterval = Timer.CANCEL
        timers.start("timer")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        assertEquals("tick_timer" to false, emitted.pop())
        assertEquals("stop_timer" to false, emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Clearing a timer cancels it`() {
        timers.start("timer")
        timers.stop("timer")
        assertEquals("start_timer" to false, emitted.pop())
        assertEquals("stop_timer" to false, emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Stopping timers emit stop`() {
        timers.start("timer")
        timers.stopAll()
        assertFalse(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        assertEquals("stop_timer" to true, emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Cleared timers are cancelled`() {
        timers.start("timer")
        timers.clearAll()
        assertFalse(timers.contains("timer"))
        assertEquals("start_timer" to false, emitted.pop())
        assertTrue(emitted.isEmpty())
        assertTrue(emitted.isEmpty())
    }
}
