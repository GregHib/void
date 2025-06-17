package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerSlotTest : TimersTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        timers = TimerSlot(events)
    }

    @Test
    fun `Overriding cancels previous timer`() {
        timers.start("1")
        timers.start("2")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("1"))
        assertTrue(timers.contains("2"))
        assertEquals(TimerStart("1"), emitted.pop())
        assertEquals(TimerStart("2"), emitted.pop())
        assertEquals(TimerStop("1", logout = false), emitted.pop())
        assertEquals(TimerTick("2"), emitted.pop())
        assertEquals(TimerTick("2"), emitted.pop())
        assertEquals(TimerTick("2"), emitted.pop())
        assertTrue(emitted.isEmpty())
    }
}
