package rs.dusk.engine.client

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
internal class SessionsTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module { single { Sessions() } })
    }

    val sessions: Sessions by inject()

    @Test
    fun `Register player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        // When
        sessions.register(session, player)
        // Then
        assert(sessions.players.containsKey(session))
        assertEquals(player, sessions.players[session])
    }

    @Test
    fun `Deregister player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        sessions.players[session] = player
        // When
        sessions.deregister(session)
        // Then
        assertFalse(sessions.players.containsKey(session))
        assertNull(sessions.players[session])
    }

    @Test
    fun `Get registered player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        sessions.players[session] = player
        // When
        val result = sessions.get(session)
        // Then
        assertEquals(player, result)
    }

    @Test
    fun `Get unregistered player`() {
        // Given
        val session: Session = mockk(relaxed = true)
        // When
        val result = sessions.get(session)
        // Then
        assertNull(result)
    }
}