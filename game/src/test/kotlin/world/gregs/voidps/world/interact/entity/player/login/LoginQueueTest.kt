package world.gregs.voidps.world.interact.entity.player.login

import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.loginQueueModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.world.script.KoinMock

internal class LoginQueueTest : KoinMock() {

    lateinit var loginQueue: LoginQueue

    override val modules = listOf(
        eventModule,
        loginQueueModule
    )

    @BeforeEach
    fun setup() {
        loginQueue = spyk(LoginQueue(25))
    }

    @Test
    fun `Login player name`() {
        val index = loginQueue.login("test", "123")

        assertEquals(1, index)
        assertEquals(1, loginQueue.logins("123"))
        assertEquals(0, loginQueue.logins("321"))
        assertTrue(loginQueue.isOnline("test"))
        assertFalse(loginQueue.isOnline("not online"))
    }

    @Test
    fun `Logout player not online`() {
        val index = loginQueue.login("test", "123")
        loginQueue.logout("test", "123", index)
        assertEquals(0, loginQueue.logins("123"))
        assertFalse(loginQueue.isOnline("test"))
    }

    @Test
    fun `Await login`() = runBlockingTest {
        launch {
            loginQueue.await()
        }
        loginQueue.run()
    }
}