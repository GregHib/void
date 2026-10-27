import com.github.michaelbull.logging.InlineLogger
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.test.KoinTest
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.engine.data.definition.MapLoader
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.LoaderGraph
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.*
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.timer.setCurrentTime
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import java.io.File
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Sets up a fully functioning game test environment (without networking)
 * The world is booted once and shared between consecutive [WorldTest] classes,
 * [WorldTestListener] shuts it down before any other test class runs.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class WorldTest : KoinTest {

    private val logger = InlineLogger()
    private val engine: GameLoop
        get() = world!!.engine
    private val accountDefs: AccountDefinitions
        get() = world!!.accountDefinitions
    private val accounts: AccountManager
        get() = world!!.accounts
    lateinit var settings: Properties
    val scripts: List<Script>
        get() = world!!.scripts

    open var loadNpcs: Boolean = false

    fun tick(times: Int = 1) = runBlocking(Contexts.Game) {
        repeat(times) {
            GameLoop.tick++
            engine.tick()
            logger.info { "Tick ${GameLoop.tick}" }
        }
    }

    fun tickIf(limit: Int = 100, block: () -> Boolean) {
        var max = limit
        while (block()) {
            if (max-- <= 0) {
                throw IllegalStateException("Exceeded tick limit $limit")
            }
            tick()
        }
    }

    fun createClient(name: String, tile: Tile = Tile.EMPTY): Pair<Player, Client> {
        val player = createPlayer(tile, name)
        val client: Client = mockk(relaxed = true)
        player.viewport = Viewport()
        player.client = client
        return player to client
    }

    fun createPlayer(tile: Tile = Tile.EMPTY, name: String = "player${Players.size}", setup: (Player) -> Unit = {}): Player {
        if (Players.any { it.accountName == name }) {
            throw IllegalStateException("Player already exists: $name")
        }
        val player = Player(tile = tile, accountName = name, passwordHash = "")
        accounts.setup(player, null, 0, viewport = true)
        assertTrue(accounts.index(player))
        accountDefs.add(player)
        tick()
        player["creation"] = -1
        player["skip_level_up"] = true
        setup(player)
        accounts.spawn(player, null)
        player.softTimers.clear("restore_stats")
        player.softTimers.clear("restore_hitpoints")
        tick()
        player.viewport?.loaded = true
        return player
    }

    fun createNPC(id: String, tile: Tile = Tile.EMPTY, block: (NPC) -> Unit = {}): NPC {
        val npc = NPCs.add(id, tile)
        block.invoke(npc)
        NPCs.run()
        return npc
    }

    fun createObject(id: String, tile: Tile = Tile.EMPTY, shape: Int = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation: Int = 0): GameObject = GameObjects.add(id, tile, shape, rotation)

    fun createFloorItem(
        id: String,
        tile: Tile = Tile.EMPTY,
        amount: Int = 1,
        revealTicks: Int = FloorItems.NEVER,
        disappearTicks: Int = FloorItems.NEVER,
        charges: Int = 0,
        owner: Player? = null,
    ): FloorItem = FloorItems.add(tile, id, amount, revealTicks, disappearTicks, charges, owner)

    fun Inventory.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount)) }

    @BeforeAll
    fun beforeAll() {
        settings = Settings.load(properties)
        if (world == null) {
            world = boot()
        }
        File(properties["storage.players.path"] as String).mkdirs()
    }

    private fun boot(): SharedWorld {
        stopKoin()
        // Other tests may have replaced the definitions so install them again
        graph.install(loaded)
        startKoin {
            printLogger(Level.ERROR)
            allowOverride(true)
            modules(
                engineModule(),
                gameModule(configFiles),
                definitionsModule(),
                gameDefinitionsModule(),
                module {
                    single(createdAtStart = true) { cache }
                    single(createdAtStart = true) { huffman }
                    single { mapDefinitions }
                    single { objectCollisionAdd }
                    single { objectCollisionRemove }
                    single {
                        Hunting(
                            get(),
                            get(),
                            object : FakeRandom() {
                                override fun nextBits(bitCount: Int) = 0
                            },
                        )
                    }
                },
            )
        }

        engineLoad()
        Wildcards.load(Settings["storage.wildcards"])
        val scripts = ContentLoader().load()
        Wildcards.clear()
        val engine: GameLoop
        val millis = measureTimeMillis {
            val tickStages = getTickStages(
                get<ConnectionQueue>(),
                get(),
                get(),
                get(),
                sequential = true,
            )
            engine = GameLoop(tickStages)
            Spawn.world(configFiles)
        }
        logger.info { "World startup took ${millis}ms" }
        for (x in 0 until 24 step 8) {
            for (y in 0 until 24 step 8) {
                Collisions.allocateIfAbsent(x, y, 0)
            }
        }
        return SharedWorld(engine, scripts, get(), get())
    }

    @BeforeEach
    fun beforeEach() {
        setCurrentTime { TIME }
        settings = Settings.load(properties)
        if (loadNpcs) {
            NPCSpawnLoader.install(npcSpawns)
            loadNpcSpawns()
        }
        setRandom(FakeRandom())
    }

    @AfterEach
    fun afterEach() {
        Players.clear()
        NPCs.clear()
        FloorItems.clear()
        GameObjects.reset()
        World.clear()
        Settings.clear()
        Instances.reset()
    }

    @AfterAll
    fun afterAll() {
        File(properties["storage.players.path"] as String).deleteRecursively()
    }

    private class SharedWorld(
        val engine: GameLoop,
        val scripts: List<Script>,
        val accountDefinitions: AccountDefinitions,
        val accounts: AccountManager,
    )

    companion object {
        private const val TIME: Long = 43_200_000 // 12:00:00
        private var world: SharedWorld? = null

        /**
         * Tear down the shared world so non-[WorldTest] classes start without koin or content scripts loaded
         */
        fun shutdown() {
            if (world == null) {
                return
            }
            world = null
            Script.clear()
            stopKoin()
        }

        private var forkLock: FileLock? = null

        /**
         * Claims a temp directory unique to this test JVM so parallel forks don't share files.
         * Slots are reused between runs so cached files (e.g. bot chat model) persist.
         */
        private val forkDirectory: String by lazy {
            generateSequence(0) { it + 1 }.firstNotNullOf { slot ->
                val dir = File("../temp/data/test-fork-$slot/")
                dir.mkdirs()
                val channel = FileChannel.open(dir.resolve(".lock").toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                val lock = channel.tryLock()
                if (lock == null) {
                    channel.close()
                    null
                } else {
                    forkLock = lock
                    "../temp/data/test-fork-$slot/"
                }
            }
        }

        private val properties: Properties by lazy {
            val properties = Properties()
            properties.load(WorldTest::class.java.getResourceAsStream("/game.properties")!!)
            for ((key, value) in properties) {
                if (value is String && value.startsWith("./")) {
                    properties[key] = value.replace("./", "../")
                }
            }
            properties["storage.players.path"] = "${forkDirectory}saves/"
            properties["storage.players.logs"] = "${forkDirectory}logs/"
            properties["storage.players.errors"] = "${forkDirectory}errors/"
            properties["storage.players.logs.seconds"] = Int.MAX_VALUE
            properties["storage.grand.exchange.offers.buy.path"] = "${forkDirectory}grand_exchange/buy_offers/"
            properties["storage.grand.exchange.offers.sell.path"] = "${forkDirectory}grand_exchange/sell_offers/"
            properties["storage.grand.exchange.offers.claim.path"] = "${forkDirectory}grand_exchange/claimable_offers.toml"
            properties["storage.grand.exchange.offers.path"] = "${forkDirectory}grand_exchange/offers.toml"
            properties["storage.grand.exchange.history.path"] = "${forkDirectory}grand_exchange/price_history/"
            // Don't write map caches as parallel forks would clash
            properties["storage.caching.active"] = false
            properties["storage.snapshots.active"] = false
            properties["storage.caching.path"] = "../data/.temp/"
            properties["bots.chat.model"] = "${forkDirectory}bot_chat.model"
            properties["quests.requirements.skipMissing"] = false
            properties["construction.freeBuild"] = false
            properties["grandExchange.priceLimit"] = true
            properties["world.npcs.randomWalk"] = false
            properties["events.shootingStars.enabled"] = false
            properties["events.livingRockCaverns.respawnTimeMinutes"] = "-1"
            properties["events.livingRockCaverns.patriarchTimeMinutes"] = "-1"
            properties["hunter.impling.respawnCycleMinutes"] = "-1"
            properties["events.penguinHideAndSeek.enabled"] = false
            properties["events.tearsOfGuthix.active"] = false
            properties["storage.autoSave.minutes"] = 0
            properties["storage.disabled"] = true
            properties["events.randomEvents.active"] = false
            properties["bots.count"] = 0
            properties.remove("world.id")
            properties.remove("world.name")
            properties
        }
        val configFiles by lazy {
            Settings.load(properties)
            configFiles()
        }
        private val cache: Cache by lazy { MemoryCache(Settings["storage.cache.path"]) }
        private val huffman: Huffman by lazy { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }

        // Maps are only loaded by the tests which need them
        private val graph: LoaderGraph by lazy { LoaderGraph((engineLoaders() + gameLoaders()).filter { it != MapLoader && it != NPCSpawnLoader }) }
        private val loaded: Map<Loader<*>, Any> by lazy { graph.run(configFiles, cache, null) }
        private val objectCollisionAdd: GameObjectCollisionAdd by lazy { GameObjectCollisionAdd() }
        private val objectCollisionRemove: GameObjectCollisionRemove by lazy { GameObjectCollisionRemove() }

        // Spawns are only needed by the tests which load npcs
        private val npcSpawns: List<NPCSpawn> by lazy { NPCSpawnLoader.load(LoadContext(configFiles, cache)) }
        private val mapDefinitions: MapDefinitions by lazy { MapDefinitions(CollisionDecoder(), cache).load() }
        val emptyTile = Tile(2655, 4640)
    }
}
