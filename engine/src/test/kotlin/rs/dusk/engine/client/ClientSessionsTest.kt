package rs.dusk.engine.client

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject
import rs.dusk.core.network.model.message.Message
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.verify.ClientVerification
import rs.dusk.engine.client.verify.clientVerificationModule
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 09, 2020
 */
internal class ClientSessionsTest : KoinMock() {

    @BeforeEach
    fun setup() {
        loadModules(module { single { ClientSessions() } }, clientVerificationModule)
    }

    val sessions: ClientSessions by inject()

    @Test
    fun `Register player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        // When
        sessions.register(session, player)
        // Then
        assert(sessions.sessions.containsKey(session))
        assertEquals(player, sessions.sessions[session])
    }

    @Test
    fun `Deregister player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        sessions.sessions[session] = player
        // When
        sessions.deregister(session)
        // Then
        assertFalse(sessions.sessions.containsKey(session))
        assertNull(sessions.sessions[session])
    }

    @Test
    fun `Get registered player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        sessions.sessions[session] = player
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

    @Test
    fun `Send player message`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        sessions.sessions[session] = player
        val message: Message = mockk(relaxed = true)
        val verification: ClientVerification = declareMock {
            every { verify(any(), player, message) } just Runs
        }
        // When
        sessions.send(session, message)
        // Then
        verify {
            verification.verify(player, message)
        }
    }

    @Test
    fun `Send unregistered message`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val session: Session = mockk(relaxed = true)
        val message: Message = mockk(relaxed = true)
        val verification: ClientVerification = declareMock {
            every { verify(player, message) }
        }
        // When
        sessions.send(session, message)
        // Then
        verify(exactly = 0) {
            verification.verify(player, message)
        }
    }
}