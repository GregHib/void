package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.test.KoinTest
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveCache
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.gameModule
import world.gregs.voidps.getTickStages
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.NetworkGatekeeper
import world.gregs.voidps.script.loadScripts
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Sets up a fully functioning game test environment (without networking)
 * Each class re-loads all config files so use sparingly
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class WorldTest : KoinTest {

    private val logger = InlineLogger()
    private lateinit var engine: GameLoop
    private lateinit var store: EventHandlerStore
    lateinit var players: Players
    private lateinit var gatekeeper: NetworkGatekeeper
    lateinit var npcs: NPCs
    lateinit var floorItems: FloorItems
    lateinit var objects: GameObjects
    private lateinit var accountDefs: AccountDefinitions
    private var saves: File? = null

    val extraProperties: MutableMap<String, Any> = mutableMapOf()

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
        val player = createPlayer(name, tile)
        val client: Client = mockk(relaxed = true)
        player.viewport = Viewport()
        player.client = client
        return player to client
    }

    fun createPlayer(name: String, tile: Tile = Tile.EMPTY): Player {
        val accounts: PlayerAccounts = get()
        val index = gatekeeper.connect(name)!!
        val player = Player(tile = tile, accountName = name, passwordHash = "")
        accounts.initPlayer(player, index)
        accountDefs.add(player)
        tick()
        player["creation"] = -1
        player["skip_level_up"] = true
        player.login(null, 0)
        player.softTimers.clear("restore_stats")
        player.softTimers.clear("restore_hitpoints")
        tick()
        player.viewport = Viewport()
        player.viewport?.loaded = true
        return player
    }

    fun createNPC(id: String, tile: Tile = Tile.EMPTY, block: (NPC) -> Unit = {}): NPC {
        val npc = npcs.add(id, tile)!!
        block.invoke(npc)
        return npc
    }

    fun createObject(id: String, tile: Tile = Tile.EMPTY, shape: Int = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation: Int = 0): GameObject {
        return objects.add(id, tile, shape, rotation)
    }

    fun createFloorItem(id: String, tile: Tile = Tile.EMPTY, amount: Int = 1, revealTicks: Int = FloorItems.NEVER, disappearTicks: Int = FloorItems.NEVER, owner: Player? = null): FloorItem {
        return floorItems.add(tile, id, amount, revealTicks, disappearTicks, owner)
    }

    fun Inventory.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount)) }

    @BeforeAll
    fun beforeAll() {
        startKoin {
            printLogger(Level.ERROR)
            fileProperties("/test.properties")
            properties(extraProperties)
            allowOverride(true)
            modules(engineModule, gameModule, module {
                single(createdAtStart = true) { cache }
                single(createdAtStart = true) { huffman }
                single(createdAtStart = true) { objectDefinitions }
                single(createdAtStart = true) { npcDefinitions }
                single(createdAtStart = true) { itemDefinitions }
                single(createdAtStart = true) { animationDefinitions }
                single(createdAtStart = true) { graphicDefinitions }
                single(createdAtStart = true) { interfaceDefinitions }
                single(createdAtStart = true) { inventoryDefinitions }
                single(createdAtStart = true) { structDefinitions }
                single(createdAtStart = true) { quickChatPhraseDefinitions }
                single(createdAtStart = true) { weaponStyleDefinitions }
                single(createdAtStart = true) { enumDefinitions }
                single(createdAtStart = true) { fontDefinitions }
                single { ammoDefinitions }
                single { parameterDefinitions }
                single { xteas }
                single { gameObjects }
                single { mapDefinitions }
                single { collisions }
                single { objectCollision }
            })
        }
        loadScripts(getProperty("scriptModule"))
        MapDefinitions(get(), get(), get()).load(active)
        saves = File(getProperty("savePath"))
        saves?.mkdirs()
        store = get()
        val millis = measureTimeMillis {
            val handler = InterfaceHandler(get(), get(), get())
            val tickStages = getTickStages(get(),
                get(),
                get(),
                get(),
                get(),
                get<ConnectionQueue>(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                handler,
                parallelPlayer = SequentialIterator())
            engine = GameLoop(tickStages, mockk(relaxed = true))
            store.populate(World)
            World.start(true)
        }
        gatekeeper = get<ConnectionGatekeeper>()
        players = get()
        npcs = get()
        floorItems = get()
        objects = get()
        accountDefs = get()
        logger.info { "World startup took ${millis}ms" }
        for (x in 0 until 24 step 8) {
            for (y in 0 until 24 step 8) {
                collisions.allocateIfAbsent(x, y, 0)
            }
        }
    }

    @BeforeEach
    fun beforeEach() {
        loadItemSpawns(floorItems, get())
        setRandom(FakeRandom())
    }

    @AfterEach
    fun afterEach() {
        gatekeeper.clear()
        players.clear()
        npcs.clear()
        floorItems.clear()
        objects.reset()
        World.clearTimers()
    }

    @AfterAll
    fun afterAll() {
        saves?.deleteRecursively()
        store.clear()
        World.shutdown()
        stopKoin()
    }

    companion object {
        private val active: File by lazy {
            ActiveCache { ActiveCache.load(getProperty("xteaPath")) }
                .checkChanges(getProperty("cachePath"), "active")
            File("../data/cache/active/")
        }
        private val cache: Cache by lazy { CacheDelegate(getProperty("cachePath")) }
        private val huffman: Huffman by lazy { Huffman().load(active.resolve(ActiveCache.indexFile(Index.HUFFMAN)).readBytes()) }
        private val ammoDefinitions: AmmoDefinitions by lazy { AmmoDefinitions().load() }
        private val parameterDefinitions: ParameterDefinitions by lazy { ParameterDefinitions(CategoryDefinitions().load(), ammoDefinitions).load() }
        private val objectDefinitions: ObjectDefinitions by lazy { ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false, parameterDefinitions).load(active)).load() }
        private val npcDefinitions: NPCDefinitions by lazy { NPCDefinitions(NPCDecoder(member = true, parameterDefinitions).load(active)).load() }
        private val itemDefinitions: ItemDefinitions by lazy { ItemDefinitions(ItemDecoder(parameterDefinitions).load(active)).load() }
        private val animationDefinitions: AnimationDefinitions by lazy { AnimationDefinitions(AnimationDecoder().load(active)).load() }
        private val graphicDefinitions: GraphicDefinitions by lazy { GraphicDefinitions(GraphicDecoder().load(active)).load() }
        private val interfaceDefinitions: InterfaceDefinitions by lazy { InterfaceDefinitions(InterfaceDecoder().load(active)).load() }
        private val inventoryDefinitions: InventoryDefinitions by lazy { InventoryDefinitions(InventoryDecoder().load(active)).load() }
        private val structDefinitions: StructDefinitions by lazy { StructDefinitions(StructDecoder(parameterDefinitions).load(active)).load() }
        private val quickChatPhraseDefinitions: QuickChatPhraseDefinitions by lazy { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(active)).load() }
        private val weaponStyleDefinitions: WeaponStyleDefinitions by lazy { WeaponStyleDefinitions().load() }
        private val enumDefinitions: EnumDefinitions by lazy { EnumDefinitions(EnumDecoder().load(active), structDefinitions).load() }
        private val collisions: Collisions by lazy { Collisions() }
        private val objectCollision: GameObjectCollision by lazy { GameObjectCollision(collisions) }
        private val xteas: Xteas by lazy { Xteas().load() }
        private val gameObjects: GameObjects by lazy { GameObjects(objectCollision, ZoneBatchUpdates(), objectDefinitions, storeUnused = true) }
        private val mapDefinitions: MapDefinitions by lazy { MapDefinitions(collisions, objectDefinitions, gameObjects).load(active) }
        private val fontDefinitions: FontDefinitions by lazy { FontDefinitions(FontDecoder().load(active)).load() }
        val emptyTile = Tile(2655, 4640)
    }
}