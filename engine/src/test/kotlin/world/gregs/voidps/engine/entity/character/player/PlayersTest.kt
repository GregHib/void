package world.gregs.voidps.engine.entity.character.player

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PlayersTest {

    private lateinit var players: Players

    @BeforeEach
    fun setup() {
        players = Players()
    }

    @Test
    fun `Add character to list`() {
        val player = Player(index = 1)
        assertTrue(players.add(player))

        assertEquals(player, players.indexed(1))
        assertEquals(1, players.size)
    }

    @Test
    fun `Remove character from list`() {
        val player = Player(index = 1)
        assertTrue(players.add(player))

        assertTrue(players.remove(player))

        assertNull(players.indexed(1))
        assertEquals(0, players.size)
    }

    @Test
    fun `Clear all characters in list`() {
        val player = Player(index = 1)
        assertTrue(players.add(player))
        players.clear()

        assertEquals(0, players.size)
    }
}