package world.gregs.voidps.engine.client

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.NetworkQueue
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountLoaderTest : KoinMock() {

    @RelaxedMockK
    private lateinit var queue: NetworkQueue

    @RelaxedMockK
    private lateinit var factory: PlayerAccounts

    @RelaxedMockK
    private lateinit var accounts: AccountDefinitions

    private lateinit var loader: PlayerAccountLoader

    @BeforeEach
    fun setup() {
        loader = spyk(PlayerAccountLoader(queue, factory, accounts, UnconfinedTestDispatcher()))
    }

    @Test
    fun `Invalid credentials`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk()
        every { player.passwordHash } returns ""
        every { factory.getOrElse("name", 2, any()) } returns player

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            client.disconnect(Response.INVALID_CREDENTIALS)
        }
    }

    @Test
    fun `Save in progress`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk()
        every { player.passwordHash } returns ""
        every { factory.saving("name") } returns true

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            client.disconnect(Response.ACCOUNT_ONLINE)
        }
    }

    @Test
    fun `Successful login`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.passwordHash } returns "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby"
        every { player.instructions } returns MutableSharedFlow()
        every { factory.getOrElse("name", 2, any()) } returns player

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            factory.login(player, client, 3)
        }
    }

}