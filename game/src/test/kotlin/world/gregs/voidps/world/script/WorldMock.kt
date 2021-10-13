package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.spawnObject
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.Shutdown
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.getGameModules
import world.gregs.voidps.getTickStages
import kotlin.system.measureTimeMillis

/**
 * Creates a testable game world without networking and a mocked cache
 * Use sparingly for abstract smoke tests only as loading all the configuration files takes time
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class WorldMock {

    private val logger = InlineLogger()
    private lateinit var definitions: InterfaceDefinitions
    private lateinit var engine: GameLoop
    lateinit var cache: Cache

    open fun loadModules(): MutableList<Module> {
        return getGameModules().toMutableList().apply {
            remove(cacheModule)
            add(mockCacheModule)
            remove(jsonPlayerModule)
            add(mockJsonPlayerModule)
            remove(cacheDefinitionModule)
            add(mockCacheDefinitionModule)
            remove(cacheConfigModule)
            add(mockCacheConfigModule)
        }
    }

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

    fun createPlayer(name: String, tile: Tile = Tile.EMPTY): Player {
        val loginQueue: LoginQueue = get()
        val factory: PlayerFactory = get()
        val index = loginQueue.connect(name)!!
        val player = Player(id = -1, tile = tile, name = name, passwordHash = "")
        factory.initPlayer(player, index)
        tick()
        player.login()
        tick()
        player.viewport.loaded = true
        return player
    }

    fun createNPC(name: String, tile: Tile = Tile.EMPTY): NPC {
        val npcs: NPCs = get()
        val npc = npcs.add(name, tile)!!
        npc.events.emit(Registered)
        return npc
    }

    fun createObject(name: String, tile: Tile = Tile.EMPTY): GameObject {
        val objectDefinitions: ObjectDefinitions = get()
        val definition = objectDefinitions.get(name)
        val gameObject = spawnObject(definition.id, tile, 0, 0)
        gameObject.events.emit(Registered)
        return gameObject
    }

    @BeforeAll
    open fun setup() {
        startKoin {
            modules(loadModules())
            fileProperties("/test.properties")
        }
        cache = get()
        val millis = measureTimeMillis {
            val tickStages = getTickStages(get(), get(), get(), get(), get(), get(), get())
            engine = GameLoop(mockk(relaxed = true), tickStages)
            get<EventHandlerStore>().populate(World)
            World.events.emit(Startup)
        }
        logger.info { "World startup took ${millis}ms" }
        definitions = get()
    }

    @BeforeEach
    fun beforeEach() = runBlocking(Dispatchers.Default) {
        clean()
    }

    private fun clean() {
        val players: Players = get()
        players.forEach {
            it.logout(false)
        }
        players.clear()
        tick(2)
        val npcs: NPCs = get()
        npcs.clear()
        val floorItems: FloorItems = get()
        floorItems.clear()
    }

    @AfterAll
    open fun teardown() {
        clean()
        World.events.emit(Shutdown)
        stopKoin()
    }
}