package world.gregs.voidps.engine.client

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.client.variable.BooleanValues
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.data.exchange.Claim
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.protocol.encode.login
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountLoaderTest : KoinMock() {

    private lateinit var queue: ConnectionQueue
    private lateinit var storage: Storage
    private lateinit var saveQueue: SaveQueue
    private lateinit var accounts: AccountManager
    private lateinit var definitions: AccountDefinitions
    private lateinit var loader: PlayerAccountLoader
    private var playerSave: PlayerSave? = null
    private val saved = mutableMapOf<String, PlayerSave>()
    private var createResult: Boolean? = null
    private var createException: Exception? = null

    @BeforeEach
    fun setup() {
        playerSave = null
        saved.clear()
        createResult = null
        createException = null
        Settings.load(mapOf("accounts.registration" to "true"))
        VariableDefinitions.set(
            mapOf(
                "display_name" to VariableDefinition.CustomVariableDefinition(StringValues, null, persistent = true),
                "choose_name" to VariableDefinition.CustomVariableDefinition(BooleanValues, null, persistent = true),
            ),
        )
        queue = mockk(relaxed = true)
        storage = object : Storage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
            }

            override fun offers(days: Int): OpenOffers = OpenOffers()

            override fun saveOffers(offers: OpenOffers) {
            }

            override fun claims(): Map<Int, Claim> = emptyMap()

            override fun saveClaims(claims: Map<Int, Claim>) {
            }

            override fun priceHistory(): Map<String, PriceHistory> = emptyMap()

            override fun savePriceHistory(history: Map<String, PriceHistory>) {
            }

            override fun saveReport(report: AbuseReport) {
            }

            override fun create(account: PlayerSave): Boolean {
                createException?.let { throw it }
                createResult?.let { return it }
                if (saved.containsKey(account.name.lowercase())) {
                    return false
                }
                saved[account.name.lowercase()] = account
                return true
            }

            override fun exists(accountName: String): Boolean = saved.containsKey(accountName.lowercase())

            override fun load(accountName: String): PlayerSave? = playerSave

            override fun accounts(): List<PlayerSave> = listOfNotNull(playerSave)
        }
        saveQueue = SaveQueue(storage, scope = TestScope())
        definitions = AccountDefinitions(mutableMapOf("name" to AccountDefinition("name", "oldName", "", "hash")), mutableMapOf("accountname" to "name"))
        accounts = mockk(relaxed = true)
        every { accounts.create(any(), any()) } answers {
            Player(tile = Tile.EMPTY, accountName = firstArg(), passwordHash = secondArg()).apply {
                this["display_name"] = "Bob"
                this["choose_name"] = true
            }
        }
        loader = PlayerAccountLoader(queue, storage, accounts, saveQueue, definitions, UnconfinedTestDispatcher())
    }

    @Test
    fun `Available when registration enabled and email unused`() {
        assertEquals(RegistrationResponse.SUCCESS, loader.available("bob@example.com"))
    }

    @Test
    fun `Refused when registration disabled`() = runTest {
        Settings.load(mapOf("accounts.registration" to "false"))

        assertEquals(RegistrationResponse.REFUSED, loader.available("bob@example.com"))
        assertEquals(RegistrationResponse.REFUSED, loader.create(Registration("bob@example.com", "hash", "localhost")))
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `Email in use when account definition exists`() {
        definitions.merge(mapOf("bob@example.com" to AccountDefinition("bob@example.com", "Bobby", "", "hash")), emptyMap()) { false }

        assertEquals(RegistrationResponse.EMAIL_IN_USE, loader.available("Bob@Example.com"))
    }

    @Test
    fun `Create persists the account and its definition`() = runTest {
        val response = loader.create(Registration("Bob.Smith@Example.com", "hash", "localhost"))

        assertEquals(RegistrationResponse.SUCCESS, response)
        val save = saved["bob.smith@example.com"]
        assertNotNull(save)
        assertEquals("bob.smith@example.com", save.name)
        assertEquals("hash", save.password)
        assertEquals("Bob", save.variables["display_name"])
        assertEquals(true, save.variables["choose_name"])
        assertEquals("Bob", definitions.getByAccount("bob.smith@example.com")?.displayName)
        assertEquals("bob.smith@example.com", definitions.get("Bob")?.accountName)
    }

    @Test
    fun `Create rejected when account already stored`() = runTest {
        saved["bob@example.com"] = PlayerSave("bob@example.com", "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), emptyMap(), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())

        assertEquals(RegistrationResponse.EMAIL_IN_USE, loader.create(Registration("bob@example.com", "other", "localhost")))
        assertEquals("hash", saved["bob@example.com"]?.password)
        assertNull(definitions.getByAccount("bob@example.com"))
        assertNull(definitions.get("Bob"))
    }

    @Test
    fun `Create unavailable when storage throws`() = runTest {
        createException = IllegalStateException("disk full")

        val response = loader.create(Registration("bob@example.com", "hash", "localhost"))

        assertEquals(RegistrationResponse.UNAVAILABLE, response)
        assertNull(definitions.getByAccount("bob@example.com"))
    }

    @Test
    fun `Second create for same email is rejected`() = runTest {
        assertEquals(RegistrationResponse.SUCCESS, loader.create(Registration("bob@example.com", "hash", "localhost")))

        assertEquals(RegistrationResponse.EMAIL_IN_USE, loader.create(Registration("bob@example.com", "other", "localhost")))
        assertEquals("hash", saved["bob@example.com"]?.password)
    }

    @Test
    fun `Get password`() {
        assertEquals("hash", loader.password("accountName"))
        assertNull(loader.password("name2"))
    }

    @Test
    fun `Successful login`() = runTest {
        val client: Client = mockk(relaxed = true)
        playerSave = PlayerSave("name", "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), emptyMap(), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
        coEvery { queue.await() } just Runs

        val instructions = loader.load(client, "name", "pass", 2)
        assertNotNull(instructions)
    }

    @Test
    fun `Can't login if banned`() = runTest {
        val client: Client = mockk(relaxed = true)
        playerSave = PlayerSave("name", "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), mapOf("banned_until" to Int.MAX_VALUE), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())

        val instructions = loader.load(client, "name", "pass", 2)
        assertNull(instructions)
        coVerify { client.disconnect(Response.ACCOUNT_DISABLED) }
    }

    @Test
    fun `Can login once ban expires`() = runTest {
        val client: Client = mockk(relaxed = true)
        playerSave = PlayerSave("name", "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), mapOf("banned_until" to 1), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
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
        every { accounts.index(any()) } returns true

        loader.connect(player, client, 2)

        coVerify {
            queue.await()
            client.login("name", 4, 0, member = false, membersWorld = false)
            accounts.spawn(player, client)
        }
    }

    @Test
    fun `Can't login while an earlier session is still in the world`() = runTest {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.LoginEncoderKt")
        mockkObject(Players)
        val client: Client = mockk(relaxed = true)
        val ghost = Player(index = 7, accountName = "name")
        every { Players.findByAccount("name") } returns ghost
        try {
            val player = Player(index = 4, accountName = "name", variables = mutableMapOf("display_name" to "name"))
            every { accounts.index(any()) } returns true

            loader.connect(player, client, 2)

            coVerify {
                accounts.logout(ghost, safely = false)
                client.disconnect(Response.ACCOUNT_ONLINE)
            }
            coVerify(exactly = 0) { accounts.spawn(player, client) }
        } finally {
            unmockkObject(Players)
        }
    }

    @Test
    fun `World full`() = runTest {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.LoginEncoderKt")
        val client: Client = mockk(relaxed = true)
        val player = Player(index = 4, accountName = "name", passwordHash = "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby", variables = mutableMapOf("display_name" to "name"))
        every { accounts.index(player) } returns false

        loader.connect(player, client, 2)

        coVerify {
            client.disconnect(Response.WORLD_FULL)
        }
    }
}
