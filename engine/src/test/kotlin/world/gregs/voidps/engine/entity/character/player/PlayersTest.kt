package world.gregs.voidps.engine.entity.character.player

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
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

    @Test
    fun `Indexed returns null for invalid player indexes`() {
        assertNull(Players.indexed(-1))
        assertNull(Players.indexed(Int.MAX_VALUE))
    }

    @Test
    fun `For each near only includes players within radius on the same level`() {
        val centre = Tile(3200, 3200)
        val near = Player(index = 1, tile = Tile(3215, 3185))
        val far = Player(index = 2, tile = Tile(3216, 3200))
        val upstairs = Player(index = 3, tile = Tile(3200, 3200, 1))
        val self = Player(index = 4, tile = centre)
        Players.add(near)
        Players.add(far)
        Players.add(upstairs)
        Players.add(self)

        val found = mutableSetOf<Player>()
        Players.forEachNear(centre, 15) { found.add(it) }

        assertEquals(setOf(near, self), found)
    }
}
