package world.gregs.voidps.engine.client

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.AbuseReport
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
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
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountCreatorTest : KoinMock() {

    private lateinit var creator: PlayerAccountCreator
    private lateinit var definitions: AccountDefinitions
    private val saved = mutableMapOf<String, PlayerSave>()
    private var createResult: Boolean? = null
    private var createException: Exception? = null

    @BeforeEach
    fun setup() {
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
        val storage = object : Storage {
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

            override fun load(accountName: String): PlayerSave? = saved[accountName.lowercase()]
        }
        val accounts: AccountManager = mockk()
        every { accounts.create(any(), any()) } answers {
            Player(tile = Tile.EMPTY, accountName = firstArg(), passwordHash = secondArg())
        }
        definitions = AccountDefinitions()
        val dispatcher = UnconfinedTestDispatcher()
        creator = PlayerAccountCreator(storage, accounts, definitions, dispatcher, dispatcher)
    }

    @Test
    fun `Available when registration enabled and email unused`() {
        assertEquals(RegistrationResponse.SUCCESS, creator.available("bob@example.com"))
    }

    @Test
    fun `Refused when registration disabled`() = runTest {
        Settings.load(mapOf("accounts.registration" to "false"))

        assertEquals(RegistrationResponse.REFUSED, creator.available("bob@example.com"))
        assertEquals(RegistrationResponse.REFUSED, creator.create(Registration("bob@example.com", "hash", "localhost")))
        assertTrue(saved.isEmpty())
    }

    @Test
    fun `Email in use when account definition exists`() {
        definitions.merge(mapOf("bob@example.com" to AccountDefinition("bob@example.com", "Bob", "", "hash")), emptyMap()) { false }

        assertEquals(RegistrationResponse.EMAIL_IN_USE, creator.available("Bob@Example.com"))
    }

    @Test
    fun `Create rejected when account already stored`() = runTest {
        saved["bob@example.com"] = save("bob@example.com")

        assertEquals(RegistrationResponse.EMAIL_IN_USE, creator.create(Registration("bob@example.com", "other", "localhost")))
        assertEquals("hash", saved["bob@example.com"]?.password)
        assertNull(definitions.getByAccount("bob@example.com"))
    }

    @Test
    fun `Create persists account with derived display name`() = runTest {
        val response = creator.create(Registration("Bob.Smith@Example.com", "hash", "localhost"))

        assertEquals(RegistrationResponse.SUCCESS, response)
        val save = saved["bob.smith@example.com"]
        assertNotNull(save)
        assertEquals("bob.smith@example.com", save.name)
        assertEquals("hash", save.password)
        assertEquals("Bob smith", save.variables["display_name"])
        assertEquals(true, save.variables["choose_name"])
        assertEquals("Bob smith", definitions.getByAccount("bob.smith@example.com")?.displayName)
        assertEquals("bob.smith@example.com", definitions.get("Bob smith")?.accountName)
    }

    @Test
    fun `Derived display name is made unique`() = runTest {
        definitions.merge(mapOf("other" to AccountDefinition("other", "Bob", "", "hash")), emptyMap()) { false }

        creator.create(Registration("bob@example.com", "hash", "localhost"))
        creator.create(Registration("bob@other.com", "hash", "localhost"))

        assertEquals("Bob2", saved["bob@example.com"]?.variables?.get("display_name"))
        assertEquals("Bob3", saved["bob@other.com"]?.variables?.get("display_name"))
    }

    @Test
    fun `Create fails when storage already has the account`() = runTest {
        createResult = false

        val response = creator.create(Registration("bob@example.com", "hash", "localhost"))

        assertEquals(RegistrationResponse.EMAIL_IN_USE, response)
        assertNull(definitions.getByAccount("bob@example.com"))
        assertNull(definitions.get("Bob"))
    }

    @Test
    fun `Create unavailable when storage throws`() = runTest {
        createException = IllegalStateException("disk full")

        val response = creator.create(Registration("bob@example.com", "hash", "localhost"))

        assertEquals(RegistrationResponse.UNAVAILABLE, response)
        assertNull(definitions.getByAccount("bob@example.com"))
    }

    @Test
    fun `Second create for same email is rejected`() = runTest {
        assertEquals(RegistrationResponse.SUCCESS, creator.create(Registration("bob@example.com", "hash", "localhost")))

        assertEquals(RegistrationResponse.EMAIL_IN_USE, creator.create(Registration("bob@example.com", "other", "localhost")))
        assertEquals("hash", saved["bob@example.com"]?.password)
    }

    private fun save(name: String) = PlayerSave(name, "hash", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), emptyMap(), emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
}
