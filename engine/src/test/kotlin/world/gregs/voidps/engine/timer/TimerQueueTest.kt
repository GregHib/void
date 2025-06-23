package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerQueueTest : TimersTest() {

    @BeforeEach
    override fun setup() {
        super.setup()
        timers = TimerQueue(events)
    }

    @Test
    fun `Multiple timers run at once`() {
        timers.start("1")
        timers.start("2")
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertTrue(timers.contains("1"))
        assertTrue(timers.contains("2"))
        assertEquals(TimerStart("1"), emitted.pop())
        assertEquals(TimerStart("2"), emitted.pop())
        repeat(3) {
            assertEquals(TimerTick("1"), emitted.pop())
            assertEquals(TimerTick("2"), emitted.pop())
        }
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Updating next timer tick changes order`() {
        block = {
            if (it is TimerStart) {
                it.interval = 2
            }
        }
        timers.start("mutable")
        block = {
            if (it is TimerStart) {
                it.interval = 3
            }
        }
        timers.start("fixed")

        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertFalse(timers.contains("timer"))
        assertEquals(TimerStart("mutable"), emitted.pop())
        assertEquals(TimerStart("fixed"), emitted.pop())
        assertEquals(TimerTick("mutable"), emitted.pop())
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `Can't run two timers with the same name`() {
        assertTrue(timers.start("1"))
        assertFalse(timers.start("1"))
    }
}
