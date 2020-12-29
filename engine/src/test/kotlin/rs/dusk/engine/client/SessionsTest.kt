package rs.dusk.engine.client

import io.mockk.mockk
import io.netty.channel.Channel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject
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
        val channel: Channel = mockk(relaxed = true)
        // When
        sessions.register(channel, player)
        // Then
        assert(sessions.players.containsKey(channel))
        assertEquals(player, sessions.players[channel])
    }

    @Test
    fun `Deregister player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val channel: Channel = mockk(relaxed = true)
        sessions.players[channel] = player
        // When
        sessions.deregister(channel)
        // Then
        assertFalse(sessions.players.containsKey(channel))
        assertNull(sessions.players[channel])
    }

    @Test
    fun `Get registered player`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val channel: Channel = mockk(relaxed = true)
        sessions.players[channel] = player
        // When
        val result = sessions.get(channel)
        // Then
        assertEquals(player, result)
    }

    @Test
    fun `Get unregistered player`() {
        // Given
        val channel: Channel = mockk(relaxed = true)
        // When
        val result = sessions.get(channel)
        // Then
        assertNull(result)
    }
}