package rs.dusk.world.entity.player.login

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.character.IndexAllocator
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.script.KoinMock
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
internal class LoginQueueTest : KoinMock() {

    lateinit var loginQueue: LoginQueue
    lateinit var bus: EventBus
    lateinit var loader: PlayerLoader
    lateinit var indexer: IndexAllocator
    lateinit var queue: Queue<Pair<Player, Login>>
    lateinit var attempts: MutableSet<String>

    override val modules = listOf(
        eventModule,
        loginQueueModule
    )

    @BeforeEach
    fun setup() {
        loader = mockk(relaxed = true)
        bus = mockk(relaxed = true)
        attempts = mutableSetOf()
        queue = LinkedList()
        indexer = mockk(relaxed = true)
        loginQueue = spyk(LoginQueue(loader, bus, 25, attempts, queue, indexer))
    }

    @Test
    fun `Successful login`() = runBlocking {
        // Given
        val session: Session = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { indexer.obtain() } returns 1
        every { loader.loadPlayer(any()) } returns player
        var result: LoginResponse? = null
        val callback = { response: LoginResponse ->
            result = response
        }
        val login = Login("Test", session, callback)
        // When
        loginQueue.add(login)?.await()
        loginQueue.tick()
        // Then
        assertEquals(LoginResponse.Success(player), result)
    }

    @Test
    fun `Login world full`() = runBlocking {
        // Given
        every { indexer.obtain() } returns null
        var result: LoginResponse? = null
        val callback = { response: LoginResponse ->
            result = response
        }
        val login = Login("Test", callback = callback)
        // When
        loginQueue.add(login)?.await()
        loginQueue.tick()
        // Then
        assertEquals(LoginResponse.WorldFull, result)
    }

    @Test
    fun `Login loading issue`() = runBlocking {
        // Given
        every { indexer.obtain() } returns 1
        every {
            loader.loadPlayer(any())
        } throws(IllegalStateException("Loading went wrong"))
        var result: LoginResponse? = null
        val callback = { response: LoginResponse ->
            result = response
        }
        val login = Login("Test", callback = callback)
        // When
        loginQueue.add(login)?.await()
        loginQueue.tick()
        // Then
        assertEquals(LoginResponse.CouldNotCompleteLogin, result)
    }

    @Test
    fun `Players are logged in request order`() = runBlocking {
        // Given
        val player1: Player = mockk(relaxed = true)
        val player2: Player = mockk(relaxed = true)
        every { loader.loadPlayer(any()) } answers {
            val name: String = arg(0)
            if(name == "Test1") player1 else player2
        }
        var first = true
        every { indexer.obtain() } answers {
            if(first) {
                first = false
                1
            } else {
                -1
            }
        }
        var result1: LoginResponse? = null
        val callback1 = { response: LoginResponse ->
            result1 = response
        }
        val login1 = Login("Test1", callback = callback1)
        var result2: LoginResponse? = null
        val callback2 = { response: LoginResponse ->
            result2 = response
        }
        val login2 = Login("Test2", callback = callback2)
        // When
        val d1 = loginQueue.add(login2)
        val d2 = loginQueue.add(login1)
        d1?.await()
        d2?.await()
        loginQueue.tick()
        // Then
        verifyOrder {
            loader.loadPlayer("Test2")
            loader.loadPlayer("Test1")
        }
        assertEquals(LoginResponse.Success(player1), result1)
        assertEquals(LoginResponse.Success(player2), result2)
    }

}