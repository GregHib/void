package world.gregs.voidps.engine.client

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.protocol.encode.login
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountLoaderTest : KoinMock() {

    private lateinit var queue: ConnectionQueue
    private lateinit var storage: AccountStorage
    private lateinit var saveQueue: SaveQueue
    private lateinit var accounts: AccountManager
    private lateinit var definitions: AccountDefinitions
    private lateinit var loader: PlayerAccountLoader
    private var playerSave: PlayerSave? = null

    @BeforeEach
    fun setup() {
        playerSave = null
        queue = mockk(relaxed = true)
        storage = object : AccountStorage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
            }

            override fun exists(accountName: String): Boolean {
                return false
            }

            override fun load(accountName: String): PlayerSave? {
                return playerSave
            }
        }
        saveQueue = SaveQueue(storage, scope = TestScope())
        definitions = AccountDefinitions(mutableMapOf("name" to AccountDefinition("name", "oldName", "", "hash")))
        accounts = mockk(relaxed = true)
        loader = PlayerAccountLoader(queue, storage, accounts, saveQueue, definitions, UnconfinedTestDispatcher())
    }

    @Test
    fun `Get password`() {
        assertEquals("hash", loader.password("name"))
        assertNull(loader.password("name2"))
    }

    @Test
    fun `Successful login`() = runTest {
        val client: Client = mockk(relaxed = true)
        playerSave = PlayerSave("name", "hash", Tile.EMPTY, doubleArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), emptyMap(), emptyMap(), emptyMap(), emptyList())
        coEvery { queue.await() } just Runs

        val instructions = loader.load(client, "name", "pass", 2)
        assertNotNull(instructions)
    }

    @Test
    fun `Can't login if account is being saved`() = runTest {
        saveQueue.save(Player(accountName = "name"))
        val client: Client = mockk(relaxed = true)

        val instructions = loader.load(client, "name", "pass", 2)
        assertNull(instructions)
        coVerify { client.disconnect(Response.ACCOUNT_ONLINE) }
    }

    @Test
    fun `Connect initiates and awaits spawn`() = runTest {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.LoginEncoderKt")
        val client: Client = mockk(relaxed = true)
        val player = Player(index = 4, accountName = "name", passwordHash = "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby", variables = mutableMapOf("display_name" to "name"))
        coEvery { queue.await() } just Runs
        every { accounts.setup(any()) } returns true

        loader.connect(player, client, 2)

        coVerify {
            queue.await()
            client.login("name", 4, 0, membersWorld = false)
            accounts.spawn(player, client, 2)
        }
    }

    @Test
    fun `World full`() = runTest {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.LoginEncoderKt")
        val client: Client = mockk(relaxed = true)
        val player = Player(index = 4, accountName = "name", passwordHash = "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby", variables = mutableMapOf("display_name" to "name"))
        every { accounts.setup(player) } returns false

        loader.connect(player, client, 2)

        coVerify {
            client.disconnect(Response.WORLD_FULL)
        }
    }

}