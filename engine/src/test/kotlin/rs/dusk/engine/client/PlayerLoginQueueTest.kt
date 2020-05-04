package rs.dusk.engine.client

import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.test.inject
import org.koin.test.mock.declareMock
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.engineModule
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.entity.factory.entityFactoryModule
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 09, 2020
 */
internal class PlayerLoginQueueTest : KoinMock() {

    val loginQueue: LoginQueue by inject()

    override val modules = listOf(clientLoginQueueModule, engineModule, entityFactoryModule)

    override val properties = listOf("loginPerTickCap" to 10)

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
        delay(10)
        loginQueue.run()
        // Then
        assertEquals(LoginResponse.Success(player), result.await())
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
        delay(10)
        loginQueue.run()
        // Then
        assertEquals(LoginResponse.Full, result.await())
    }

    @Test
    fun `Login loading issue`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        declareMock<PlayerFactory> {
            every {
                spawn(
                    any(),
                    any()
                )
            } returns GlobalScope.async { throw IllegalStateException("Loading went wrong") }
        }
        // When
        val result = loginQueue.add("Test", session)
        delay(10)
        loginQueue.run()
        // Then
        assertEquals(LoginResponse.Failure, result.await())
    }

    @Test
    fun `Add suspends`() = runBlocking {
        // Given
        declareMock<PlayerFactory> {
            every { spawn(any(), any()) } returns GlobalScope.async { null }
        }
        // When
        val result = loginQueue.add("Test")
        delay(10)
        loginQueue.run()
        // Then
        assertEquals(LoginResponse.Full, result.await())
    }

    @Test
    fun `Players are logged in request order`() = runBlocking {
        // Given
        val player: Player = mockk(relaxed = true)
        val factory = declareMock<PlayerFactory> {
            every { spawn(any(), any()) } answers {
                val name: String = arg(0)
                GlobalScope.async { if (name == "Test1") player else null }
            }
        }
        val test2 = loginQueue.add("Test2")
        delay(25)
        val test1 = loginQueue.add("Test1")
        // When
        delay(100)
        loginQueue.run()
        // Then
        delay(50)
        coVerifyOrder {
            factory.spawn("Test2", null)
            factory.spawn("Test1", null)
        }
        assertEquals(LoginResponse.Success(player), test1.await())
        assertEquals(LoginResponse.Full, test2.await())
    }

}