package world.gregs.voidps.engine.entity.character.player.req

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player

internal class RequestTest {

    lateinit var player: Player
    lateinit var player2: Player
    lateinit var player3: Player

    @BeforeEach
    fun setup() {
        player = Player(accountName = "player1")
        player2 = Player(accountName = "player2")
        player3 = Player(accountName = "player3")
    }

    @Test
    fun `Player request`() {
        val request: (Player, Player) -> Unit = { _, _ -> }
        assertTrue(player.request(player2, "trade", request))
    }

    @Test
    fun `Has player requested`() {
        val request: (Player, Player) -> Unit = { _, _ -> }
        player.request(player2, "trade", request)
        assertTrue(player.hasRequest(player2, "trade"))
    }

    @Test
    fun `Get request`() {
        val request: (Player, Player) -> Unit = { _, _ -> }
        player.request(player2, "trade", request)
        assertTrue(player.hasRequest(player2, "trade"))
        assertFalse(player.hasRequest(player3, "trade"))
        assertFalse(player.hasRequest(player3, "other"))
    }

    @Test
    fun `Remove request`() {
        val request: (Player, Player) -> Unit = { _, _ -> }
        player.request(player2, "trade", request)
        player.removeRequest(player2, "trade")
        assertFalse(player.hasRequest(player3, "trade"))
    }

    @Test
    fun `Invoke and removed if add target with matching request`() {
        var called = false
        val request: (Player, Player) -> Unit = { a, b ->
            called = true
            assertTrue(a.hasRequest(b, "trade"))
            assertTrue(b.hasRequest(a, "trade"))
        }
        player2.request(player, "trade", request)
        assertFalse(player.request(player2, "trade", request))

        assertTrue(called)
        assertFalse(player.hasRequest(player2, "trade"))
        assertFalse(player2.hasRequest(player, "trade"))
    }
}
