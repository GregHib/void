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

/**
 * @author GregHib <greg@gregs.world>
 * @since April 09, 2020
 */
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
        val index = loginQueue.login("test")

        assertEquals(1, index)
        assertTrue(loginQueue.isOnline("test"))
        assertFalse(loginQueue.isOnline("not online"))
    }

    @Test
    fun `Logout player not online`() {
        val index = loginQueue.login("test")
        loginQueue.logout("test", index)
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