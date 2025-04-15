import com.github.michaelbull.logging.InlineLogger
import content.entity.obj.ObjectTeleports
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
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.engineModule
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
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.entity.character.npc.loadNpcSpawns
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.entity.item.drop.DropTables
import java.io.File
import java.util.*
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
    lateinit var settings: Properties

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

    fun createPlayer(name: String = "player", tile: Tile = Tile.EMPTY): Player {
        val player = Player(tile = tile, accountName = name, passwordHash = "")
        assertTrue(accounts.setup(player, null, 0))
        accountDefs.add(player)
        tick()
        player["creation"] = -1
        player["skip_level_up"] = true
        accounts.spawn(player, null)
        player.softTimers.clear("restore_stats")
        player.softTimers.clear("restore_hitpoints")
        tick()
        player.viewport = Viewport()
        player.viewport?.loaded = true
        return player
    }

    fun createNPC(id: String, tile: Tile = Tile.EMPTY, block: (NPC) -> Unit = {}): NPC {
        val npc = npcs.add(id, tile)
        block.invoke(npc)
        npcs.run()
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
        settings = Settings.load(properties)
        stopKoin()
        startKoin {
            printLogger(Level.ERROR)
            allowOverride(true)
            modules(engineModule(files), gameModule(files), module {
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
                single(createdAtStart = true) { objectTeleports }
                single(createdAtStart = true) { itemOnItemDefinitions }
                single(createdAtStart = true) { variableDefinitions }
                single(createdAtStart = true) { dropTables }
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
        ContentLoader.load()
        MapDefinitions(CollisionDecoder(get()), get(), get(), cache).loadCache()
        saves = File(Settings["storage.players.path"])
        saves?.mkdirs()
        val millis = measureTimeMillis {
            val tickStages = getTickStages(
                get(),
                get(),
                get(),
                get(),
                get(),
                get<ConnectionQueue>(),
                get(),
                get(),
                get(),
                sequential = true
            )
            engine = GameLoop(tickStages)

            World.start(files)
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
        settings = Settings.load(properties)
        loadItemSpawns(floorItems, get(), files.list(Settings["spawns.items"]), itemDefinitions)
        if (loadNpcs) {
            loadNpcSpawns(npcs, files.list(Settings["spawns.npcs"]), npcDefinitions)
        }
        loadObjectSpawns(objects, files.list(Settings["spawns.objects"]), objectDefinitions)
        setRandom(FakeRandom())
    }

    @AfterEach
    fun afterEach() {
        players.clear()
        npcs.clear()
        floorItems.clear()
        objects.reset()
        World.clear()
        Settings.clear()
    }

    @AfterAll
    fun afterAll() {
        saves?.deleteRecursively()
        Events.events.clear()
        World.shutdown()
        stopKoin()
    }

    companion object {
        private val properties: Properties by lazy {
            val properties = Properties()
            properties.load(WorldTest::class.java.getResourceAsStream("/game.properties")!!)
            for ((key, value) in properties) {
                if (value is String && value.startsWith("./")) {
                    properties[key] = value.replace("./", "../")
                }
            }
            properties["storage.players.path"] = "../data/test-saves/"
            properties["world.npcs.randomWalk"] = false
            properties["bots.count"] = 0
            properties.remove("world.id")
            properties.remove("world.name")
            properties
        }
        private val files by lazy {
            Settings.load(properties)
            configFiles()
        }
        private val cache: Cache by lazy { MemoryCache(Settings["storage.cache.path"]) }
        private val huffman: Huffman by lazy { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
        private val ammoDefinitions: AmmoDefinitions by lazy { AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"])) }
        private val parameterDefinitions: ParameterDefinitions by lazy { ParameterDefinitions(CategoryDefinitions().load(files.find(Settings["definitions.categories"])), ammoDefinitions).load(files.find(Settings["definitions.parameters"])) }
        private val objectDefinitions: ObjectDefinitions by lazy {
            ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false, parameterDefinitions).load(cache)).load(files.list(Settings["definitions.objects"]))
        }
        private val npcDefinitions: NPCDefinitions by lazy {
            NPCDefinitions(NPCDecoder(member = true, parameterDefinitions).load(cache)).load(files.list(Settings["definitions.npcs"]))
        }
        private val itemDefinitions: ItemDefinitions by lazy {
            ItemDefinitions(ItemDecoder(parameterDefinitions).load(cache)).load(files.list(Settings["definitions.items"]))
        }
        private val animationDefinitions: AnimationDefinitions by lazy {
            AnimationDefinitions(AnimationDecoder().load(cache)).load(files.list(Settings["definitions.animations"]))
        }
        private val graphicDefinitions: GraphicDefinitions by lazy {
            GraphicDefinitions(GraphicDecoder().load(cache)).load(files.list(Settings["definitions.graphics"]))
        }
        private val interfaceDefinitions: InterfaceDefinitions by lazy {
            InterfaceDefinitions(InterfaceDecoder().load(cache)).load(files.list(Settings["definitions.interfaces"]), files.find(Settings["definitions.interfaces.types"]))
        }
        private val inventoryDefinitions: InventoryDefinitions by lazy {
            InventoryDefinitions(InventoryDecoder().load(cache)).load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"]), itemDefinitions)
        }
        private val structDefinitions: StructDefinitions by lazy { StructDefinitions(StructDecoder(parameterDefinitions).load(cache)).load(files.find(Settings["definitions.structs"])) }
        private val quickChatPhraseDefinitions: QuickChatPhraseDefinitions by lazy { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load() }
        private val weaponStyleDefinitions: WeaponStyleDefinitions by lazy { WeaponStyleDefinitions().load(files.find(Settings["definitions.weapons.styles"])) }
        private val weaponAnimationDefinitions: WeaponAnimationDefinitions by lazy { WeaponAnimationDefinitions().load(files.find(Settings["definitions.weapons.animations"])) }
        private val enumDefinitions: EnumDefinitions by lazy { EnumDefinitions(EnumDecoder().load(cache), structDefinitions).load(files.find(Settings["definitions.enums"])) }
        private val collisions: Collisions by lazy { Collisions() }
        private val objectCollisionAdd: GameObjectCollisionAdd by lazy { GameObjectCollisionAdd(collisions) }
        private val objectCollisionRemove: GameObjectCollisionRemove by lazy { GameObjectCollisionRemove(collisions) }
        private val gameObjects: GameObjects by lazy { GameObjects(objectCollisionAdd, objectCollisionRemove, ZoneBatchUpdates(), objectDefinitions, storeUnused = true) }
        private val mapDefinitions: MapDefinitions by lazy { MapDefinitions(CollisionDecoder(collisions), objectDefinitions, gameObjects, cache).loadCache() }
        private val fontDefinitions: FontDefinitions by lazy { FontDefinitions(FontDecoder().load(cache)).load(files.find(Settings["definitions.fonts"])) }
        private val objectTeleports: ObjectTeleports by lazy { ObjectTeleports().load(files.list(Settings["map.teleports"])) }
        private val itemOnItemDefinitions: ItemOnItemDefinitions by lazy { ItemOnItemDefinitions().load(files.list(Settings["definitions.itemOnItem"])) }
        private val variableDefinitions: VariableDefinitions by lazy {
            VariableDefinitions().load(
                files.list(Settings["definitions.variables.players"]),
                files.list(Settings["definitions.variables.bits"]),
                files.list(Settings["definitions.variables.clients"]),
                files.list(Settings["definitions.variables.strings"]),
                files.list(Settings["definitions.variables.customs"]),
            )
        }
        private val dropTables: DropTables by lazy { DropTables().load(files.list(Settings["spawns.drops"]), get()) }
        val emptyTile = Tile(2655, 4640)
    }
}