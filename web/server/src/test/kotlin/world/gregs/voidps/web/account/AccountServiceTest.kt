package world.gregs.voidps.web.account

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.data.AbuseReport
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.OfflineStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.config.VariableDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.data.exchange.Claim
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.type.Tile
import world.gregs.voidps.web.api.ApiException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountServiceTest {

    private val definitions = AccountDefinitions()
    private lateinit var loader: PlayerAccountLoader
    private lateinit var accounts: AccountManager
    private lateinit var saveQueue: SaveQueue
    private lateinit var service: AccountService
    private val saved = mutableMapOf<String, PlayerSave>()
    private var online: Player? = null

    @BeforeEach
    fun setup() {
        saved.clear()
        startKoin {
            modules(module { single { definitions } })
        }
        Settings.load(mapOf("web.api.registration" to "true", "storage.disabled" to "false"))
        VariableDefinitions.set(
            mapOf(
                "display_name" to VariableDefinition.CustomVariableDefinition(StringValues, null, persistent = true),
                "rights" to VariableDefinition.CustomVariableDefinition(StringValues, null, persistent = true),
            ),
        )
        loader = mockk()
        coEvery { loader.create(any(), any(), any(), any()) } answers {
            val name = firstArg<String>()
            account(name, thirdArg<String?>() ?: name)
            RegistrationResponse.SUCCESS
        }
        accounts = mockk(relaxed = true)
        val files = object : Storage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
                for (account in accounts) {
                    saved[account.name] = account
                }
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

            override fun create(account: PlayerSave): Boolean = false

            override fun exists(accountName: String): Boolean = saved.containsKey(accountName)

            override fun load(accountName: String): PlayerSave? = saved[accountName]

            override fun accounts(): List<PlayerSave> = saved.values.toList()
        }
        saveQueue = SaveQueue(files)
        val dispatcher = UnconfinedTestDispatcher()
        val storage = AccountStorage(OfflineStorage(files), saveQueue, definitions, dispatcher, dispatcher)
        service = AccountService(loader, storage, accounts, definitions, RegistrationLimiter(2, 60_000L) { 0L })
    }

    @AfterEach
    fun teardown() {
        online?.let { Players.remove(it) }
        online = null
        stopKoin()
    }

    /**
     * An existing account known to the server and, unless [offline] is false, saved in storage
     */
    private fun account(accountName: String, displayName: String, vararg variables: Pair<String, Any>, offline: Boolean = true) {
        definitions.merge(mapOf(accountName to AccountDefinition(accountName, displayName, "", "hash")), emptyMap()) { false }
        if (offline) {
            saved[accountName] = PlayerSave(accountName, "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), mapOf("display_name" to displayName, *variables), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
        }
    }

    private fun login(accountName: String, displayName: String = accountName): Player {
        account(accountName, displayName)
        val player = Player(accountName = accountName, passwordHash = "hash", index = 1)
        player["display_name"] = displayName
        Players.add(player)
        online = player
        return player
    }

    private suspend fun assertField(field: String, block: suspend () -> Unit) {
        val exception = assertThrows<ApiException.Validation> { block() }
        assertEquals(field, exception.details.single().field)
    }

    @Test
    fun `Username create hashes password and normalises name`() = runTest {
        val account = service.create("Bob_1", "hunter22", null, "203.0.113.9")

        assertEquals("Bob 1", account.accountName)
        assertEquals("Bob 1", account.displayName)
        coVerify { loader.create("Bob 1", match { BCrypt.checkpw("hunter22", it) }, null, "203.0.113.9") }
    }

    @Test
    fun `Username display name must match`() = runTest {
        assertField("displayName") { service.create("Bob", "hunter22", "Bobby", "ip") }
        service.create("Bob", "hunter22", "Bob", "ip")

        coVerify(exactly = 1) { loader.create("Bob", any(), null, "ip") }
    }

    @Test
    fun `Email create requires a display name`() = runTest {
        assertField("displayName") { service.create("bob@example.com", "hunter22", null, "ip") }
        assertField("displayName") { service.create("bob@example.com", "hunter22", "Bob!", "ip") }
        val account = service.create("Bob@Example.com", "hunter22", "Bob_cat", "ip")

        assertEquals("bob@example.com", account.accountName)
        assertEquals("Bob cat", account.displayName)
        coVerify(exactly = 1) { loader.create("bob@example.com", any(), "Bob cat", "ip") }
    }

    @Test
    fun `Invalid names and passwords are rejected before hashing`() = runTest {
        assertField("name") { service.create("Bob!", "hunter22", null, "ip") }
        assertField("name") { service.create("Thirteen chars", "hunter22", null, "ip") }
        assertField("name") { service.create("bob@", "hunter22", "Bob", "ip") }
        assertField("password") { service.create("Bob", "bob", null, "ip") }
        assertField("password") { service.create("Bob", "pass word", null, "ip") }

        coVerify(exactly = 0) { loader.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Registration can be disabled`() = runTest {
        Settings.load(mapOf("web.api.registration" to "false"))

        assertThrows<ApiException.Forbidden> { service.create("Bob", "hunter22", null, "ip") }
    }

    @Test
    fun `Rate limit counts well-formed requests per address`() = runTest {
        assertField("password") { service.create("Bob", "no", null, "ip") }
        assertField("password") { service.create("Bob", "no", null, "ip") }
        service.create("Bob", "hunter22", null, "ip")
        service.create("Bob2", "hunter22", null, "ip")
        val limited = assertThrows<ApiException.RateLimited> { service.create("Bob3", "hunter22", null, "ip") }
        assertEquals(60, limited.retryAfterSeconds)
        service.create("Bob3", "hunter22", null, "other")

        coVerify(exactly = 0) { loader.create("Bob3", any(), any(), "ip") }
    }

    @Test
    fun `Loader responses are mapped`() = runTest {
        coEvery { loader.create(any(), any(), any(), any()) } returns RegistrationResponse.EMAIL_IN_USE
        assertEquals("name_taken", assertThrows<ApiException.Conflict> { service.create("Bob", "hunter22", null, "ip") }.code)

        coEvery { loader.create(any(), any(), any(), any()) } returns RegistrationResponse.UNAVAILABLE
        assertThrows<ApiException.Unavailable> { service.create("Bob", "hunter22", null, "ip") }
    }

    @Test
    fun `Password is validated then hashed into storage`() = runTest {
        account("Bob", "Bob")

        assertField("password") { service.password("Bob", "bob") }
        service.password("Bob", "newpass1")

        assertTrue(BCrypt.checkpw("newpass1", saved["Bob"]!!.password))
        assertTrue(BCrypt.checkpw("newpass1", definitions.getByAccount("Bob")!!.passwordHash))
        coVerify(exactly = 0) { accounts.logout(any(), any()) }
    }

    @Test
    fun `Online password change logs the player out`() = runTest {
        val player = login("Bob")

        service.password("Bob", "newpass1")

        assertTrue(BCrypt.checkpw("newpass1", player.passwordHash))
        assertTrue(saveQueue.saving("Bob"))
        coVerify(exactly = 1) { accounts.logout(player, false) }
    }

    @Test
    fun `Rename offline account`() = runTest {
        account("bob@example.com", "Bob")

        val account = service.rename("Bob@Example.com", "Bobby__Two")

        assertEquals("Bobby Two", account.displayName)
        assertEquals("Bob", account.previousName)
        val save = saved["bob@example.com"]!!
        assertEquals("Bobby Two", save.variables["display_name"])
        assertEquals(listOf("Bob"), save.variables["name_history"])
        assertNull(definitions.get("Bob"))
        assertEquals("bob@example.com", definitions.get("Bobby Two")?.accountName)
    }

    @Test
    fun `Rename online player`() = runTest {
        val player = login("Bob")

        service.rename("Bob", "Bobby")

        assertEquals("Bobby", player.name)
        assertEquals("Bob", definitions.get("Bobby")?.accountName)
        assertTrue(saveQueue.saving("Bob"))
        coVerify(exactly = 0) { accounts.logout(any(), any()) }
    }

    @Test
    fun `Rename validates the new name`() = runTest {
        account("Bob", "Bob")
        account("alice@example.com", "Alice")
        account("Carol", "Caz")

        assertField("displayName") { service.rename("Bob", "Bob!") }
        assertField("displayName") { service.rename("Bob", "Thirteen chars") }
        assertEquals("name_taken", assertThrows<ApiException.Conflict> { service.rename("Bob", "alice") }.code)
        assertEquals("name_taken", assertThrows<ApiException.Conflict> { service.rename("Bob", "Carol") }.code)
        service.rename("Bob", "bob")

        assertEquals("bob", saved["Bob"]?.variables?.get("display_name"))
    }

    @Test
    fun `Accounts missing from storage are not found`() = runTest {
        account("Bob", "Bob", offline = false)

        assertEquals("account_not_found", assertThrows<ApiException.NotFound> { service.rename("Bob", "Bobby") }.code)
        assertThrows<ApiException.NotFound> { service.password("Bob", "newpass1") }
        assertEquals("hash", definitions.getByAccount("Bob")?.passwordHash)
    }

    @Test
    fun `Password and rename only reach accounts by account name`() = runTest {
        account("bob@example.com", "Bob Cat")

        assertThrows<ApiException.NotFound> { service.password("Bob Cat", "newpass1") }
        assertThrows<ApiException.NotFound> { service.rename("Bob Cat", "Bobby") }
        service.password("Bob@Example.com", "newpass1")

        assertTrue(BCrypt.checkpw("newpass1", saved["bob@example.com"]!!.password))
    }

    @Test
    fun `Unknown accounts are not found`() = runTest {
        assertThrows<ApiException.NotFound> { service.password("alice", "newpass1") }
        assertThrows<ApiException.NotFound> { service.rename("alice", "Alicia") }
    }

    @Test
    fun `Offline staff accounts are forbidden`() = runTest {
        account("Mod Bob", "Mod Bob", "rights" to "mod")

        assertThrows<ApiException.Forbidden> { service.password("Mod Bob", "newpass1") }
        assertThrows<ApiException.Forbidden> { service.rename("Mod Bob", "Bobby") }
        assertEquals("hash", saved["Mod Bob"]?.password)
        assertEquals("Mod Bob", saved["Mod Bob"]?.variables?.get("display_name"))
        assertEquals("Mod Bob", definitions.get("Mod Bob")?.accountName)
    }

    @Test
    fun `Online staff accounts are forbidden`() = runTest {
        val player = login("Mod Bob")
        player.rights = PlayerRights.Admin

        assertThrows<ApiException.Forbidden> { service.password("Mod Bob", "newpass1") }
        assertThrows<ApiException.Forbidden> { service.rename("Mod Bob", "Bobby") }
        assertEquals("hash", player.passwordHash)
        assertEquals("Mod Bob", player.name)
        assertFalse(saveQueue.saving("Mod Bob"))
        coVerify(exactly = 0) { accounts.logout(any(), any()) }
    }

    @Test
    fun `Account lookup is by account name only`() {
        account("bob@example.com", "Bob")
        account("Api Bob", "Bobby")

        val account = service.account("BOB@EXAMPLE.COM")
        assertNotNull(account)
        assertEquals("bob@example.com", account.accountName)
        assertEquals("Bob", account.displayName)
        assertEquals("Api Bob", service.account("Api_Bob")?.accountName)
        assertNull(service.account("bob"))
        assertNull(service.account("alice"))
    }

    @Test
    fun `Names are unavailable when used as an account or display name`() = runTest {
        account("bob@example.com", "Bob")
        account("Api Bob", "Bobby")

        assertFalse(service.available("bob"))
        assertFalse(service.available("Bob@Example.com"))
        assertFalse(service.available("api_bob"))
        assertFalse(service.available("BOBBY"))
        assertTrue(service.available("Alice"))
        assertTrue(service.available("alice@example.com"))
        assertField("name") { service.available("Bob!") }
    }
}
