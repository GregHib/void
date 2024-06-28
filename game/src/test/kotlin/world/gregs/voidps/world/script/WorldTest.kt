package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.test.KoinTest
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.Main
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.gameModule
import world.gregs.voidps.getTickStages
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.script.loadScripts
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns
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
    lateinit var players: Players
    lateinit var npcs: NPCs
    lateinit var floorItems: FloorItems
    lateinit var objects: GameObjects
    private lateinit var accountDefs: AccountDefinitions
    private lateinit var accounts: AccountManager
    private var saves: File? = null

    val extraProperties: MutableMap<String, Any> = mutableMapOf()

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
        val player = createPlayer(name, tile)
        val client: Client = mockk(relaxed = true)
        player.viewport = Viewport()
        player.client = client
        return player to client
    }

    fun createPlayer(name: String, tile: Tile = Tile.EMPTY): Player {
        val player = Player(tile = tile, accountName = name, passwordHash = "")
        assertTrue(accounts.setup(player))
        accountDefs.add(player)
        tick()
        player["creation"] = -1
        player["skip_level_up"] = true
        accounts.spawn(player, null, 0)
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

    fun createFloorItem(
        id: String,
        tile: Tile = Tile.EMPTY,
        amount: Int = 1,
        revealTicks: Int = FloorItems.NEVER,
        disappearTicks: Int = FloorItems.NEVER,
        charges: Int = 0,
        owner: Player? = null
    ): FloorItem {
        return floorItems.add(tile, id, amount, revealTicks, disappearTicks, charges, owner)
    }

    fun Inventory.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount)) }

    @BeforeAll
    fun beforeAll() {
        Main.name = "test"
        stopKoin()
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
                single(createdAtStart = true) { weaponAnimationDefinitions }
                single(createdAtStart = true) { enumDefinitions }
                single(createdAtStart = true) { fontDefinitions }
                single { ammoDefinitions }
                single { parameterDefinitions }
                single { gameObjects }
                single { mapDefinitions }
                single { collisions }
                single { objectCollisionAdd }
                single { objectCollisionAdd }
                single { objectCollisionRemove }
                single {
                    Hunting(get(), get(), get(), get(), get(), get(), object : FakeRandom() {
                        override fun nextBits(bitCount: Int) = 0
                    })
                }
            })
        }
        loadScripts()
        MapDefinitions(CollisionDecoder(get()), get(), get(), cache).loadCache()
        saves = File(getProperty("savePath"))
        saves?.mkdirs()
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
                get(),
                handler,
                sequential = true)
            engine = GameLoop(tickStages)
            World.start(true)
        }
        players = get()
        npcs = get()
        floorItems = get()
        objects = get()
        accountDefs = get()
        accounts = get()
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
        if (loadNpcs) {
            loadNpcSpawns(npcs)
        }
        loadObjectSpawns(objects)
        setRandom(FakeRandom())
    }

    @AfterEach
    fun afterEach() {
        players.clear()
        npcs.clear()
        floorItems.clear()
        objects.reset()
        World.clear()
    }

    @AfterAll
    fun afterAll() {
        saves?.deleteRecursively()
        Events.events.clear()
        World.shutdown()
        stopKoin()
    }

    companion object {
        private val cache: Cache by lazy { MemoryCache(getProperty("cachePath")) }
        private val huffman: Huffman by lazy { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
        private val ammoDefinitions: AmmoDefinitions by lazy { AmmoDefinitions().load() }
        private val parameterDefinitions: ParameterDefinitions by lazy { ParameterDefinitions(CategoryDefinitions().load(), ammoDefinitions).load() }
        private val objectDefinitions: ObjectDefinitions by lazy { ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false, parameterDefinitions).load(cache)).load() }
        private val npcDefinitions: NPCDefinitions by lazy { NPCDefinitions(NPCDecoder(member = true, parameterDefinitions).load(cache)).load() }
        private val itemDefinitions: ItemDefinitions by lazy { ItemDefinitions(ItemDecoder(parameterDefinitions).load(cache)).load() }
        private val animationDefinitions: AnimationDefinitions by lazy { AnimationDefinitions(AnimationDecoder().load(cache)).load() }
        private val graphicDefinitions: GraphicDefinitions by lazy { GraphicDefinitions(GraphicDecoder().load(cache)).load() }
        private val interfaceDefinitions: InterfaceDefinitions by lazy { InterfaceDefinitions(InterfaceDecoder().load(cache)).load() }
        private val inventoryDefinitions: InventoryDefinitions by lazy { InventoryDefinitions(InventoryDecoder().load(cache)).load() }
        private val structDefinitions: StructDefinitions by lazy { StructDefinitions(StructDecoder(parameterDefinitions).load(cache)).load() }
        private val quickChatPhraseDefinitions: QuickChatPhraseDefinitions by lazy { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load() }
        private val weaponStyleDefinitions: WeaponStyleDefinitions by lazy { WeaponStyleDefinitions().load() }
        private val weaponAnimationDefinitions: WeaponAnimationDefinitions by lazy { WeaponAnimationDefinitions().load() }
        private val enumDefinitions: EnumDefinitions by lazy { EnumDefinitions(EnumDecoder().load(cache), structDefinitions).load() }
        private val collisions: Collisions by lazy { Collisions() }
        private val objectCollisionAdd: GameObjectCollisionAdd by lazy { GameObjectCollisionAdd(collisions) }
        private val objectCollisionRemove: GameObjectCollisionRemove by lazy { GameObjectCollisionRemove(collisions) }
        private val gameObjects: GameObjects by lazy { GameObjects(objectCollisionAdd, objectCollisionRemove, ZoneBatchUpdates(), objectDefinitions, storeUnused = true) }
        private val mapDefinitions: MapDefinitions by lazy { MapDefinitions(CollisionDecoder(collisions), objectDefinitions, gameObjects, cache).loadCache() }
        private val fontDefinitions: FontDefinitions by lazy { FontDefinitions(FontDecoder().load(cache)).load() }
        val emptyTile = Tile(2655, 4640)
    }
}