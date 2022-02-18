package world.gregs.voidps.engine.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.NetworkGatekeeper

internal class ConnectionGatekeeperTest : KoinMock() {

    private lateinit var gatekeeper: NetworkGatekeeper
    private lateinit var players: Players

    override val modules = listOf(
        eventModule,
        clientConnectionModule
    )

    @BeforeEach
    fun setup() {
        players = mockk(relaxed = true)
        every { players.indexer } returns IndexAllocator(5)
        gatekeeper = spyk(ConnectionGatekeeper(players))
    }

    @Test
    fun `Login player name`() {
        val index = gatekeeper.connect("test", "123")

        assertEquals(1, index)
        assertEquals(1, gatekeeper.connections("123"))
        assertEquals(0, gatekeeper.connections("321"))
        assertTrue(gatekeeper.connected("test"))
        assertFalse(gatekeeper.connected("not online"))
    }

    @Test
    fun `Logout player not online`() {
        val index = gatekeeper.connect("test", "123")
        gatekeeper.disconnect("test", "123", index)
        assertEquals(0, gatekeeper.connections("123"))
        assertFalse(gatekeeper.connected("test"))
    }
}