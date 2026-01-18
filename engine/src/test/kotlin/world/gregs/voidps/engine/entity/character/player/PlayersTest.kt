package world.gregs.voidps.engine.entity.character.player

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PlayersTest {

    @BeforeEach
    fun setup() {
        Players.clear()
    }

    @Test
    fun `Add character to list`() {
        val player = Player(index = 1)
        assertTrue(Players.add(player))

        assertEquals(player, Players.indexed(1))
        assertEquals(1, Players.size)
    }

    @Test
    fun `Remove character from list`() {
        val player = Player(index = 1)
        assertTrue(Players.add(player))

        assertTrue(Players.remove(player))

        assertNull(Players.indexed(1))
        assertEquals(0, Players.size)
    }

    @Test
    fun `Clear all characters in list`() {
        val player = Player(index = 1)
        assertTrue(Players.add(player))
        Players.clear()

        assertEquals(0, Players.size)
    }
}
