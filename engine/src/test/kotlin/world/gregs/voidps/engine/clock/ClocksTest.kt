package world.gregs.voidps.engine.clock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.Values

internal class ClocksTest {

    lateinit var clocks: Clocks

    @BeforeEach
    fun setup() {
        clocks = Clocks(ValueDelegate(Values()))
        GameLoop.tick = 0
    }

    @Test
    fun `Clock tracks ticks and remaining time`() {
        clocks.start("new_clock", 5)
        GameLoop.tick += 4
        assertTrue(clocks.contains("new_clock"))
        assertEquals(1, clocks.remaining("new_clock"))
    }

    @Test
    fun `Clock which has finished is removed`() {
        clocks.start("new_clock", 5)
        GameLoop.tick += 5
        assertFalse(clocks.contains("new_clock"))
        assertEquals(0, clocks.remaining("new_clock"))
        GameLoop.tick += 2
        assertEquals(-1, clocks.remaining("new_clock"))
    }

    @Test
    fun `Can stop clock midway through`() {
        clocks.start("new_clock", 5)
        GameLoop.tick++
        clocks.stop("new_clock")
        assertFalse(clocks.contains("new_clock"))
        assertEquals(-1, clocks.remaining("new_clock"))
    }

    @Test
    fun `Clocks can be infinite`() {
        clocks.start("inf_clock")
        assertTrue(clocks.contains("inf_clock"))
        clocks.stop("inf_clock")
        assertFalse(clocks.contains("inf_clock"))
        assertEquals(-1, clocks.remaining("inf_clock"))
    }

    @Test
    fun `Infinite clocks can be toggled`() {
        clocks.toggle("inf_clock")
        assertTrue(clocks.contains("inf_clock"))
        clocks.toggle("inf_clock")
        assertFalse(clocks.contains("inf_clock"))
    }
}