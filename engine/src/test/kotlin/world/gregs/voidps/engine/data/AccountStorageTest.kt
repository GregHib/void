package world.gregs.voidps.engine.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
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
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountStorageTest : KoinMock() {

    private val definitions = AccountDefinitions()

    override val modules = listOf(
        module {
            single { definitions }
        },
    )

    private lateinit var storage: AccountStorage
    private lateinit var saveQueue: SaveQueue
    private val saved = mutableMapOf<String, PlayerSave>()
    private var writes = 0
    private var onSave: (() -> Unit)? = null
    private var online: Player? = null

    private val password: (PlayerSave) -> PlayerSave = { it.copy(password = "new") }
    private val none: suspend (Player) -> Unit = { fail("Player ${it.accountName} shouldn't be online") }

    @BeforeEach
    fun setup() {
        saved.clear()
        writes = 0
        onSave = null
        Settings.load(mapOf("storage.disabled" to "false"))
        VariableDefinitions.set(mapOf("display_name" to VariableDefinition.CustomVariableDefinition(StringValues, null, persistent = true)))
        val dispatcher = UnconfinedTestDispatcher()
        val files = object : Storage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
                writes++
                for (account in accounts) {
                    saved[account.name] = account
                }
                onSave?.invoke()
                onSave = null
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
        saveQueue = SaveQueue(files, scope = CoroutineScope(dispatcher))
        definitions.merge(mapOf("bob" to AccountDefinition("bob", "Bob", "", "old")), emptyMap()) { false }
        saved["bob"] = save("bob", mapOf("display_name" to "Bob"))
        storage = AccountStorage(OfflineStorage(files), saveQueue, definitions, dispatcher, dispatcher)
    }

    @AfterEach
    fun teardown() {
        online?.let { Players.remove(it) }
        online = null
    }

    @Test
    fun `Offline account is edited in storage`() = runTest {
        assertTrue(storage.edit("bob", none, password))

        assertEquals("new", saved["bob"]?.password)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertEquals(1, writes)
        assertFalse(saveQueue.saving("bob"))
    }

    @Test
    fun `Online player is edited in memory and queued for saving`() = runTest {
        val player = login()

        assertTrue(storage.edit("bob", { it.passwordHash = "new" }) { fail("Player is online") })

        assertEquals("new", player.passwordHash)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertTrue(saveQueue.saving("bob"))
        assertEquals(0, writes)
    }

    @Test
    fun `Pending save is edited instead of storage`() = runTest {
        saveQueue.save(Player(accountName = "bob", passwordHash = "old"))

        assertTrue(storage.edit("bob", none, password))

        assertEquals("old", saved["bob"]?.password)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        saveQueue.run()
        assertEquals("new", saved["bob"]?.password)
        assertEquals(1, writes)
    }

    @Test
    fun `Player logging in during the write is edited too`() = runTest {
        onSave = { login() }

        assertTrue(storage.edit("bob", { it.passwordHash = "new" }, password))

        assertEquals("new", online?.passwordHash)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertTrue(saveQueue.saving("bob"))
    }

    @Test
    fun `Player logging out during the write has their pending save edited`() = runTest {
        onSave = { saveQueue.save(Player(accountName = "bob", passwordHash = "old")) }

        assertTrue(storage.edit("bob", none, password))

        saveQueue.run()
        assertEquals("new", saved["bob"]?.password)
        assertEquals("new", definitions.getByAccount("bob")?.passwordHash)
        assertEquals(2, writes)
    }

    @Test
    fun `Display name change updates the definition`() = runTest {
        assertTrue(storage.edit("bob", none) { it.copy(variables = it.variables + ("display_name" to "Bobby")) })

        assertNull(definitions.get("Bob"))
        assertEquals("bob", definitions.get("Bobby")?.accountName)
        assertEquals("Bob", definitions.getByAccount("bob")?.previousName)
        assertEquals("Bobby", definitions.getByAccount("bob")?.displayName)
    }

    @Test
    fun `Online display name change updates the definition`() = runTest {
        val player = login()

        assertTrue(storage.edit("bob", { it.name = "Bobby" }) { fail("Player is online") })

        assertEquals("bob", definitions.get("Bobby")?.accountName)
        assertEquals("Bob", definitions.getByAccount("bob")?.previousName)
        assertEquals("Bobby", player.name)
    }

    @Test
    fun `Unknown account is not found`() = runTest {
        assertFalse(storage.edit("alice", none, password))
        saved.remove("bob")
        assertFalse(storage.edit("bob", none, password))

        assertEquals("old", definitions.getByAccount("bob")?.passwordHash)
        assertEquals(0, writes)
    }

    @Test
    fun `Account name is matched by definition`() = runTest {
        assertTrue(storage.edit("BOB", none, password))

        assertEquals("new", saved["bob"]?.password)
    }

    @Test
    fun `Failed edit leaves the account unchanged`() = runTest {
        assertThrows<IllegalStateException> { storage.edit("bob", none) { error("refused") } }
        val player = login()
        assertThrows<IllegalStateException> { storage.edit("bob", { error("refused") }, password) }

        assertEquals("old", saved["bob"]?.password)
        assertEquals("old", player.passwordHash)
        assertEquals("old", definitions.getByAccount("bob")?.passwordHash)
        assertFalse(saveQueue.saving("bob"))
    }

    private fun login(): Player {
        val player = Player(accountName = "bob", passwordHash = "old", index = Players.index()!!)
        player["display_name"] = "Bob"
        Players.add(player)
        online = player
        return player
    }

    private fun save(name: String, variables: Map<String, Any>) = PlayerSave(name, "old", Tile.EMPTY, intArrayOf(), emptyList(), intArrayOf(), true, intArrayOf(), intArrayOf(), variables, emptyMap(), emptyMap(), emptyList(), arrayOf(), emptyList(), emptyMap(), emptyMap(), emptyList())
}
