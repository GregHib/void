package world.gregs.voidps.engine.client

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.ClientSession

/**
 * @author GregHib <greg@gregs.world>
 * @since April 09, 2020
 */
internal class SessionsTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module { single { Sessions() } })
    }

    private val sessions: Sessions by inject()

    @Test
    fun `Register player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: ClientSession = mockk(relaxed = true)
        // When
        sessions.register(session, player)
        // Then
        assert(sessions.contains(player))
        assertEquals(session, sessions.get(player))
    }

    @Test
    fun `Deregister player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: ClientSession = mockk(relaxed = true)
        sessions.sessions[player] = session
        // When
        sessions.deregister(player)
        // Then
        assertFalse(sessions.contains(player))
        assertNull(sessions.get(player))
    }

    @Test
    fun `Get unregistered player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        // When
        val result = sessions.get(player)
        // Then
        assertNull(result)
    }
}