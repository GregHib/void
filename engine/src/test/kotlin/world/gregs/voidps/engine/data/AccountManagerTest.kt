package world.gregs.voidps.engine.data

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.get
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.protocol.encode.logout
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class AccountManagerTest : KoinMock() {

    private lateinit var manager: AccountManager
    private lateinit var connectionQueue: ConnectionQueue

    override val modules = listOf(module {
        single { ItemDefinitions(emptyArray()) }
        single { InterfaceDefinitions(emptyArray()).apply { ids = emptyMap() } }
        single { AreaDefinitions(areas = mapOf(0 to setOf(AreaDefinition("area", Rectangle(Tile(0), 1, 1), emptySet())))) }
    })

    @BeforeEach
    fun setup() {
        val inventoryDefinitions = InventoryDefinitions(arrayOf(InventoryDefinition.EMPTY))
        inventoryDefinitions.ids = mapOf("worn_equipment" to 0)
        connectionQueue = ConnectionQueue(1)
        val storage = object : AccountStorage {
            override fun names(): Map<String, AccountDefinition> = emptyMap()

            override fun clans(): Map<String, Clan> = emptyMap()

            override fun save(accounts: List<PlayerSave>) {
            }

            override fun exists(accountName: String): Boolean = false

            override fun load(accountName: String): PlayerSave? = null
        }
        Settings.load(mapOf("world.home.x" to "1234", "world.home.y" to "5432", "world.experienceRate" to "1.0"))
        manager = AccountManager(
            interfaceDefinitions = get(),
            inventoryDefinitions = inventoryDefinitions,
            itemDefinitions = get(),
            accountDefinitions = AccountDefinitions(),
            collisionStrategyProvider = CollisionStrategyProvider(),
            variableDefinitions = VariableDefinitions(),
            saveQueue = SaveQueue(storage),
            connectionQueue = connectionQueue,
            areaDefinitions = get(),
            players = Players()
        )
    }

    @Test
    fun `Create a new player`() {
        val start = System.currentTimeMillis()
        val player = manager.create("name", "hash")
        assertTrue(player["creation", 0L] >= start)
        assertTrue(player["new_player", false])
        assertEquals(Tile(1234, 5432), player.tile)
    }

    @Test
    fun `Initialise player`() {
        val player = Player(0)
        manager.setup(player, null, 0)
        assertNotNull(player.visuals)
        assertNotNull(player.interfaces)
        assertNotNull(player.interfaceOptions)
        assertNotNull(player.options)
        assertNotNull(player.collision)
    }

    @Test
    fun `Spawn player`() {
        val player = Player(0)
        player.interfaces = Interfaces(player, definitions = get())
        val client: Client = mockk(relaxed = true)
        manager.spawn(player, client)
        verify {
            client.onDisconnecting(any())
        }
    }

    @Test
    fun `Despawn player`() {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.LogoutEncoderKt")
        val client: Client = mockk(relaxed = true)
        val player = Player(0)
        player.client = client

        manager.logout(player, true)
        connectionQueue.run()
        GameLoop.tick = 2
        World.run()

        verify {
            client.logout()
            client.disconnect()
        }
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }
}