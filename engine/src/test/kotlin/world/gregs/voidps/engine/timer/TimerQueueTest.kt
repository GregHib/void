package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop

internal class TimerQueueTest : TimersTest() {

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
        timers = TimerQueue()
    }

    @Test
    fun `Multiple timers run at once`() {
        var count = 0L
        timers.add("", 0) {
            count++
        }
        timers.add("", 0) {
            count++
        }
        repeat(3) {
            timers.run()
            GameLoop.tick++
        }
        assertEquals(6, count)
    }
}