package rs.dusk.engine.client

import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.inject
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 09, 2020
 */
internal class LoginQueueTaskTest : KoinMock() {

    val loginQueue: LoginQueue by inject()
    lateinit var factory: PlayerFactory
    lateinit var task: LoginQueueTask

    override val modules = listOf(
        loginQueueModule,
        module { single { factory } }
    )

    @BeforeEach
    fun setup() {
        factory = mockk(relaxed = true)
        task = spyk(LoginQueueTask(loginQueue, 10))
    }

    @Test
    fun `Successful login`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { factory.spawn(any(), any(), any()) } returns async { player }
        // When
        val result = loginQueue.add("Test", session)
        delay(10)
        task.run()
        // Then
        assertEquals(LoginResponse.Success(player), result.await())
    }

    @Test
    fun `Login world full`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        every { factory.spawn(any(), any(), any()) } returns async { null }
        // When
        val result = loginQueue.add("Test", session)
        delay(10)
        task.run()
        // Then
        assertEquals(LoginResponse.Full, result.await())
    }

    @Test
    fun `Login loading issue`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        every {
            factory.spawn(any(), any(), any())
        } returns GlobalScope.async { throw IllegalStateException("Loading went wrong") }
        // When
        val result = loginQueue.add("Test", session)
        delay(10)
        task.run()
        // Then
        assertEquals(LoginResponse.Failure, result.await())
    }

    @Test
    fun `Add suspends`() = runBlocking {
        // Given
        every { factory.spawn(any(), any(), any()) } returns GlobalScope.async { null }
        // When
        val result = loginQueue.add("Test")
        delay(10)
        task.run()
        // Then
        assertEquals(LoginResponse.Full, result.await())
    }

    @Test
    fun `Players are logged in request order`() = runBlocking {
        // Given
        val player: Player = mockk(relaxed = true)
        every { factory.spawn(any(), any(), any()) } answers {
            val name: String = arg(0)
            GlobalScope.async { if (name == "Test1") player else null }
        }
        val test2 = loginQueue.add("Test2")
        delay(25)
        val test1 = loginQueue.add("Test1")
        // When
        delay(100)
        task.run()
        // Then
        delay(50)
        coVerifyOrder {
            factory.spawn("Test2", null, null)
            factory.spawn("Test1", null, null)
        }
        assertEquals(LoginResponse.Success(player), test1.await())
        assertEquals(LoginResponse.Full, test2.await())
    }

}