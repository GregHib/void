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
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.file.Maps
import world.gregs.voidps.gameModule
import world.gregs.voidps.getTickStages
import world.gregs.voidps.network.Client
import world.gregs.voidps.postCacheGameModule
import world.gregs.voidps.script.loadScripts
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns
import world.gregs.voidps.world.interact.world.spawn.stairsModule
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
    private lateinit var players: Players
    private lateinit var npcs: NPCs
    lateinit var floorItems: FloorItems
    private lateinit var objects: CustomObjects
    private lateinit var accountDefs: AccountDefinitions
    private lateinit var collisions: Collisions
    private var saves: File? = null

    open val properties: String = "/test.properties"

    fun tick(times: Int = 1) = runBlocking(Contexts.Game) {
        repeat(times) {
            engine.tick()
            GameLoop.tick++
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
        val gatekeeper: ConnectionGatekeeper = get()
        val factory: PlayerFactory = get()
        val index = gatekeeper.connect(name)!!
        val player = Player(tile = tile, accountName = name, passwordHash = "").apply {
            this["creation", true] = 0
            this["skip_level_up"] = true
        }
        factory.initPlayer(player, index)
        accountDefs.add(player)
        tick()
        player.login(null, 0, collisions, players)
        tick()
        player.viewport = Viewport()
        player.viewport?.loaded = true
        return player
    }

    fun createNPC(id: String, tile: Tile = Tile.EMPTY, block: (NPC) -> Unit = {}): NPC {
        val npcs: NPCs = get()
        val npc = npcs.add(id, tile)!!
        block.invoke(npc)
        return npc
    }

    fun createObject(id: String, tile: Tile = Tile.EMPTY): GameObject {
        val gameObject = spawnObject(id, tile, 0, 0)
        gameObject.events.emit(Registered)
        return gameObject
    }

    fun Container.set(index: Int, id: String, amount: Int = 1) = transaction { set(index, Item(id, amount)) }

    @BeforeAll
    fun beforeAll() {
        startKoin {
            printLogger(Level.ERROR)
            fileProperties(properties)
            allowOverride(true)
            modules(engineModule, stairsModule, musicModule, gameModule)
            modules(module {
                single(createdAtStart = true) { cache }
                single(createdAtStart = true) { huffman }
                single(createdAtStart = true) { objectDefinitions }
                single(createdAtStart = true) { npcDefinitions }
                single(createdAtStart = true) { itemDefinitions }
                single(createdAtStart = true) { animationDefinitions }
                single(createdAtStart = true) { graphicDefinitions }
                single(createdAtStart = true) { interfaceDefinitions }
                single(createdAtStart = true) { containerDefinitions }
                single(createdAtStart = true) { enumDefinitions }
                single(createdAtStart = true) { structDefinitions }
                single(createdAtStart = true) { quickChatPhraseDefinitions }
                single(createdAtStart = true) { styleDefinitions }
            })
            modules(postCacheModule, postCacheGameModule)
        }
        loadScripts(getProperty("scriptModule"))
        Maps(cache, get(), get(), get(), get(), get(), get(), get()).load()
        saves = File(getProperty("savePath"))
        saves?.mkdirs()
        store = get()
        val millis = measureTimeMillis {
            val handler = InterfaceHandler(get(), get(), get())
            val tickStages = getTickStages(get(),
                get(),
                get(),
                get(),
                get<ConnectionQueue>(),
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
        players = get()
        npcs = get()
        floorItems = get()
        objects = get()
        accountDefs = get()
        collisions = get()
        logger.info { "World startup took ${millis}ms" }
    }

    @BeforeEach
    fun beforeEach() {
        loadObjectSpawns(objects, get())
        loadItemSpawns(floorItems)
    }

    @AfterEach
    fun afterEach() {
        players.clear()
        npcs.clear()
        floorItems.clear()
        objects.clear()
        World.timers.clearAll()
    }

    @AfterAll
    fun afterAll() {
        saves?.deleteRecursively()
        store.clear()
        World.shutdown()
        stopKoin()
    }

    companion object {
        private val cache: Cache by lazy { CacheDelegate(getProperty("cachePath")) }
        private val huffman: Huffman by lazy { Huffman(cache.getFile(Indices.HUFFMAN, 1)!!) }
        private val objectDefinitions: ObjectDefinitions by lazy { ObjectDefinitions(ObjectDecoder(cache, member = true, lowDetail = false)).load() }
        private val npcDefinitions: NPCDefinitions by lazy { NPCDefinitions(NPCDecoder(cache, member = true)).load() }
        private val itemDefinitions: ItemDefinitions by lazy { ItemDefinitions(ItemDecoder(cache)).load() }
        private val animationDefinitions: AnimationDefinitions by lazy { AnimationDefinitions(AnimationDecoder(cache)).load() }
        private val graphicDefinitions: GraphicDefinitions by lazy { GraphicDefinitions(GraphicDecoder(cache)).load() }
        private val interfaceDefinitions: InterfaceDefinitions by lazy { InterfaceDefinitions(InterfaceDecoder(cache)).load() }
        private val containerDefinitions: ContainerDefinitions by lazy { ContainerDefinitions(ContainerDecoder(cache)).load() }
        private val structDefinitions: StructDefinitions by lazy { StructDefinitions(StructDecoder(cache)).load() }
        private val enumDefinitions: EnumDefinitions by lazy { EnumDefinitions(EnumDecoder(cache), structDefinitions).load() }
        private val quickChatPhraseDefinitions: QuickChatPhraseDefinitions by lazy { QuickChatPhraseDefinitions(QuickChatPhraseDecoder(cache)).load() }
        private val styleDefinitions: StyleDefinitions by lazy { StyleDefinitions().load(ClientScriptDecoder(cache, revision634 = true)) }

        val emptyTile = Tile(2655, 4640)
    }
}