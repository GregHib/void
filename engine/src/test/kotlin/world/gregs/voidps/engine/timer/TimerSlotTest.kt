package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerSlotTest : TimersTest() {

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
        timers = TimerSlot()
    }

    @Test
    fun `Overriding cancels previous timer`() {
        var count = 0L
        val t1 = timers.add(0) {
            count++
        }
        val t2 = timers.add(0) {
            count++
        }
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertEquals(3, count)
        assertTrue(t1.cancelled)
        assertFalse(t2.cancelled)
    }
}