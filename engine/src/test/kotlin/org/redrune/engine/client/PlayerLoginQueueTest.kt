package org.redrune.engine.client

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.inject
import org.koin.test.mock.declareMock
import org.redrune.core.network.model.session.Session
import org.redrune.engine.entity.factory.PlayerFactory
import org.redrune.engine.entity.model.Player
import org.redrune.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 09, 2020
 */
internal class PlayerLoginQueueTest : KoinMock() {
    @BeforeEach
    fun setup() {
        loadModules(clientLoginQueueModule)
    }

    val loginQueue: LoginQueue by inject()

    @Test
    fun `Successful login`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        declareMock<PlayerFactory> {
            every { spawn(any(), any()) } returns async { player }
        }
        // When
        val result = loginQueue.add("Test", session)
        // Then
        assert(result is LoginResponse.Success)
        result as LoginResponse.Success
        assertEquals(player, result.player)
    }

    @Test
    fun `Login world full`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        declareMock<PlayerFactory> {
            every { spawn(any(), any()) } returns async { null }
        }
        // When
        val result = loginQueue.add("Test", session)
        // Then
        assert(result is LoginResponse.Full)
    }

    @Test
    fun `Login loading issue`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        declareMock<PlayerFactory> {
            every { spawn(any(), any()) } returns GlobalScope.async { throw IllegalStateException("Loading went wrong") }
        }
        // When
        val result = loginQueue.add("Test", session)
        // Then
        assert(result is LoginResponse.Failure)
    }

}