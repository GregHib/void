package world.gregs.voidps.engine.timer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import kotlin.test.assertFalse

internal class TimerTest {

    @BeforeEach
    fun setup() {
        GameLoop.tick = 0
    }

    @Test
    fun `Reset tick to next interval`() {
        val timer = Timer("", 2)
        assertEquals(2, timer.nextTick)
        GameLoop.tick = 2
        timer.reset()
        assertEquals(4, timer.nextTick)
    }

    @Test
    fun `Timer not ready if next tick less than current`() {
        val timer = Timer("", 2)
        GameLoop.tick++
        assertFalse(timer.ready())
    }

    @Test
    fun `Timer ready if next tick greater or equal to current`() {
        val timer = Timer("", 2)
        GameLoop.tick += 2
        assertTrue(timer.ready())
        GameLoop.tick++
        assertTrue(timer.ready())
    }
}
