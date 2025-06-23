package world.gregs.voidps.engine.client.variable

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.player.Player

internal class ClocksTest {

    lateinit var variables: Variables
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        variables = Variables(player, mutableMapOf())
        player.variables = variables
        GameLoop.tick = 0
    }

    @Test
    fun `Clock tracks ticks and remaining time`() {
        player.start("new_clock", 5)
        GameLoop.tick += 4
        assertTrue(player.hasClock("new_clock"))
        assertEquals(1, player.remaining("new_clock"))
    }

    @Test
    fun `Clock which has finished is removed`() {
        player.start("new_clock", 5)
        GameLoop.tick += 5
        assertFalse(player.hasClock("new_clock"))
        assertEquals(0, player.remaining("new_clock"))
        GameLoop.tick += 2
        assertEquals(-1, player.remaining("new_clock"))
    }

    @Test
    fun `Can stop clock midway through`() {
        player.start("new_clock", 5)
        GameLoop.tick++
        player.stop("new_clock")
        assertFalse(player.hasClock("new_clock"))
        assertEquals(-1, player.remaining("new_clock"))
    }

    @Test
    fun `Unset clocks returns invalid duration`() {
        assertFalse(player.hasClock("inf_clock"))
        assertEquals(-1, player.remaining("inf_clock"))
    }
}
