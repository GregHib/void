package world.gregs.voidps.engine.client

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountLoaderTest : KoinMock() {

    private lateinit var queue: ConnectionQueue
    private lateinit var accounts: PlayerAccounts
    private lateinit var definitions: AccountDefinitions
    private lateinit var loader: PlayerAccountLoader

    @BeforeEach
    fun setup() {
        queue = mockk(relaxed = true)
        accounts = mockk(relaxed = true)
        definitions = AccountDefinitions()
        val indexer = IndexAllocator(10)
        loader = PlayerAccountLoader(queue, accounts, definitions, indexer, UnconfinedTestDispatcher())
    }

    @Test
    fun `Save in progress`() = runTest {
        val client: Client = mockk(relaxed = true)
        every { accounts.saving("name") } returns true

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            client.disconnect(Response.ACCOUNT_ONLINE)
        }
    }

    @Test
    fun `Successful login`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player = Player(accountName = "name", passwordHash = "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby", variables = mutableMapOf("display_name" to "name"))
        every { accounts.get("name") } returns player
        coEvery { queue.await() } just Runs

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            accounts.spawn(player, client, 3)
        }
    }

}