package world.gregs.voidps.engine.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.variable.StringValues
import world.gregs.voidps.engine.data.AbuseReport
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
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountUpdaterTest : KoinMock() {

    private val definitions = AccountDefinitions()

    override val modules = listOf(
        module {
            single { definitions }
        },
    )

    private lateinit var updater: PlayerAccountUpdater
    private lateinit var saveQueue: SaveQueue
    private val saved = mutableMapOf<String, PlayerSave>()
    private var writes = 0
    private var loadException: Exception? = null
    private var online: Player? = null

    @BeforeEach
    fun setup() {
        saved.clear()
        writes = 0
        loadException = null
        Settings.load(mapOf("storage.disabled" to "false"))
        VariableDefinitions.set(
            mapOf(
                "display_name" to VariableDefinition.CustomVariableDefinition(StringValues, null, persistent = true),
            ),
        )
        val storage = object : Storage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
                writes++
                for (account in accounts) {
                    saved[account.name.lowercase()] = account
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

            override fun exists(accountName: String): Boolean = saved.containsKey(accountName.lowercase())

            override fun load(accountName: String): PlayerSave? {
                loadException?.let { throw it }
                return saved[accountName.lowercase()]
            }
        }
        saveQueue = SaveQueue(storage)
        definitions.merge(mapOf("bob" to AccountDefinition("bob", "Bob", "", "old")), emptyMap()) { false }
        saved["bob"] = save("bob", mapOf("display_name" to "Bob"))
        val dispatcher = UnconfinedTestDispatcher()
        updater = PlayerAccountUpdater(storage, definitions, saveQueue, dispatcher, dispatcher)
    }

    @AfterEach
    fun teardown() {
        online?.let { Players.remove(it) }
        online = null
    }

    @Test
    fun `Password of offline account is written to storage`() = runTest {
        val result = updater.password("bob", "new")

        assertEquals(AccountUpdate.SUCCESS, result)
        assertEquals("new", saved["bob"]?.password)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertEquals(1, writes)
        assertFalse(saveQueue.saving("bob"))
    }

    @Test
    fun `Password of online player is queued for saving`() = runTest {
        val player = login()

        val result = updater.password("bob", "new")

        assertEquals(AccountUpdate.SUCCESS, result)
        assertEquals("new", player.passwordHash)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertTrue(saveQueue.saving("bob"))
        assertEquals(0, writes)
    }

    @Test
    fun `Busy while a save is in flight`() = runTest {
        saveQueue.save(Player(accountName = "bob", passwordHash = "old"))

        assertEquals(AccountUpdate.BUSY, updater.password("bob", "new"))
        assertEquals("old", definitions.getByAccount("bob")?.passwordHash)
    }

    @Test
    fun `Unknown account is not found`() = runTest {
        assertEquals(AccountUpdate.NOT_FOUND, updater.password("alice", "new"))
        saved.remove("bob")
        assertEquals(AccountUpdate.NOT_FOUND, updater.password("bob", "new"))
        assertEquals("old", definitions.getByAccount("bob")?.passwordHash)
    }

    @Test
    fun `Storage failure is unavailable`() = runTest {
        loadException = IllegalStateException("disk full")

        assertEquals(AccountUpdate.UNAVAILABLE, updater.password("bob", "new"))
        assertEquals("old", definitions.getByAccount("bob")?.passwordHash)
    }

    @Test
    fun `Rename offline account`() = runTest {
        val result = updater.rename("bob", "Bobby")

        assertEquals(AccountUpdate.SUCCESS, result)
        val save = saved["bob"]!!
        assertEquals("Bobby", save.variables["display_name"])
        assertEquals(listOf("Bob"), save.variables["name_history"])
        assertNull(definitions.get("Bob"))
        assertEquals("bob", definitions.get("Bobby")?.accountName)
        assertEquals("Bob", definitions.getByAccount("bob")?.previousName)
    }

    @Test
    fun `Rename online player`() = runTest {
        val player = login()
        player["display_name"] = "Bob"

        val result = updater.rename("bob", "Bobby")

        assertEquals(AccountUpdate.SUCCESS, result)
        assertEquals("Bobby", player.name)
        assertEquals("bob", definitions.get("Bobby")?.accountName)
        assertTrue(saveQueue.saving("bob"))
    }

    @Test
    fun `Rename to a taken name`() = runTest {
        definitions.merge(mapOf("alice" to AccountDefinition("alice", "Alice", "", "hash")), emptyMap()) { false }

        assertEquals(AccountUpdate.TAKEN, updater.rename("bob", "alice"))
        assertEquals("Bob", saved["bob"]?.variables?.get("display_name"))
    }

    @Test
    fun `Rename to an invalid name`() = runTest {
        assertEquals(AccountUpdate.INVALID, updater.rename("bob", "Bob!"))
        assertEquals(AccountUpdate.INVALID, updater.rename("bob", "Thirteen chars"))
        assertEquals(0, writes)
    }

    private fun login(): Player {
        val player = Player(accountName = "bob", passwordHash = "old", index = 1)
        Players.add(player)
        online = player
        return player
    }

    private fun save(name: String, variables: Map<String, Any>) = PlayerSave(name, "old", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), variables, emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
}
