package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.test.KoinTest
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.getGameModules
import world.gregs.voidps.getTickStages
import world.gregs.voidps.network.Client
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
    private var saves: File? = null

    open val properties: String = "/test.properties"

    fun tick(times: Int = 1) {
        repeat(times) {
            engine.run()
        }
    }

    fun tickIf(limit: Int = 100, block: () -> Boolean) {
        var max = limit
        while (block()) {
            if (max-- <= 0) {
                break
            }
            tick()
        }
    }

    fun createClient(name: String, tile: Tile = Tile.EMPTY): Pair<Player, Client> {
        val player = createPlayer(name, tile)
        val client: Client = mockk(relaxed = true)
        player.client = client
        return player to client
    }

    fun createPlayer(name: String, tile: Tile = Tile.EMPTY): Player {
        val gatekeeper: ConnectionGatekeeper = get()
        val factory: PlayerFactory = get()
        val index = gatekeeper.connect(name)!!
        val player = Player(tile = tile, accountName = name, passwordHash = "").apply {
            this["creation", true] = 0
            get<AccountDefinitions>().add(this)
        }
        factory.initPlayer(player, index)
        tick()
        player.login()
        tick()
        player.viewport.loaded = true
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

    @BeforeAll
    fun beforeAll() {
        saves = File("../data/saves/")
        saves?.mkdirs()
        startKoin {
            printLogger(Level.ERROR)
            fileProperties(properties)
            allowOverride(true)
            modules(getGameModules())
            modules(module {
                single(createdAtStart = true) {
                    cache
                }
            })
        }
        store = get()
        val millis = measureTimeMillis {
            val tickStages = getTickStages(get(), get(), get<ConnectionQueue>(), get(), get(), get(), get())
            engine = GameLoop(mockk(relaxed = true), tickStages)
            store.populate(World)
            World.events.emit(Startup)
        }
        players = get()
        npcs = get()
        floorItems = get()
        objects = get()
        logger.info { "World startup took ${millis}ms" }
    }

    @AfterEach
    fun afterEach() {
        players.clear()
        npcs.clear()
        floorItems.clear()
        // TODO clear custom objects
    }

    @AfterAll
    fun afterAll() {
        saves?.deleteRecursively()
        store.clear()
        World.shutdown()
        stopKoin()
    }

    companion object {
        private var cache: Cache = CacheDelegate("../data/cache/")
        val emptyTile = Tile(2655, 4640)
    }
}