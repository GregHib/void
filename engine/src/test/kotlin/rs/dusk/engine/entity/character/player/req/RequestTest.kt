package rs.dusk.engine.entity.character.player.req

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.character.player.Player

internal class RequestTest {

    lateinit var player: Player
    lateinit var player2: Player
    lateinit var player3: Player
    lateinit var requests: Requests

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        player2 = mockk(relaxed = true)
        player3 = mockk(relaxed = true)
        requests = Requests(player)
        every { player.requests } returns requests
    }

    @Test
    fun `Player request`() {
        val request: (Player, Player) -> Unit = mockk()
        assertTrue(requests.add(player2, "trade", request))
    }

    @Test
    fun `Has player requested`() {
        val request: (Player, Player) -> Unit = mockk()
        requests.add(player2, "trade", request)
        assertTrue(requests.has(player2))
    }

    @Test
    fun `Get request`() {
        val request: (Player, Player) -> Unit = mockk()
        requests.add(player2, "trade", request)
        assertEquals(request, requests.getOrNull(player2, "trade"))
        assertNull(requests.getOrNull(player3, "trade"))
        assertNull(requests.getOrNull(player3, "other"))
    }

    @Test
    fun `Remove request`() {
        val request: (Player, Player) -> Unit = mockk()
        requests.add(player2, "trade", request)
        requests.remove(player2, "trade")
        assertNull(requests.getOrNull(player3, "trade"))
    }

    @Test
    fun `Remove all requests`() {
        val request: (Player, Player) -> Unit = mockk()
        requests.add(player2, "trade", request)
        assertTrue(requests.removeAll(player2))
        assertNull(requests.getOrNull(player3, "trade"))
    }

    @Test
    fun `Remove all returns false if not set`() {
        assertFalse(requests.removeAll(player2))
    }

    @Test
    fun `If player has request request is invoked and request removed`() {
        val request: (Player, Player) -> Unit = mockk()
        every { request.invoke(any(), any()) } just Runs
        every { player2.requests.has(player, "trade") } returns true
        every { player2.requests.getOrNull(player, "trade") } returns null
        every { player2.requests.remove(player, "trade") } returns true
        assertFalse(requests.add(player2, "trade", request))

        verify {
            request.invoke(player, player2)
            player2.requests.remove(player, "trade")
        }
    }
}