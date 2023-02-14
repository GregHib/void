package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerTest {

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
    }

    @Test
    fun `Resume sets next interval`() {
        val timer = Timer(2) {}
        assertEquals(2, timer.nextTick)
        GameLoop.tick = 2
        timer.resume()
        assertEquals(1, timer.count)
        assertEquals(4, timer.nextTick)
    }

    @Test
    fun `Cancel timer`() {
        val timer = Timer(2) {}
        timer.cancel()
        assertTrue(timer.cancelled)
        assertEquals(-1, timer.nextTick)
        assertEquals(-1, timer.count)
    }

    @Test
    fun `Cancel invokes block if cancelExecution is set`() {
        var called = false
        val timer = Timer(2, callOnCancel = true) {
            called = true
        }
        timer.cancel()
        assertTrue(called)
        assertTrue(timer.cancelled)
    }
}