package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.get
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.getGameModules
import world.gregs.voidps.getTickStages
import kotlin.system.measureTimeMillis

abstract class WorldMock : KoinMock() {

    private val logger = InlineLogger()
    private lateinit var definitions: InterfaceDefinitions
    val cache: Cache = mockk(relaxed = true)

    init {
        every { cache.getFile(any(), archive = any(), file = any()) } returns null
        every { cache.getFile(any(), name = any(), xtea = any()) } returns null
        every { cache.getFile(Indices.ENUMS, archive = any(), file = any()) } returns byteArrayOf(5, 0, 0, 0)
    }

    open fun loadModules(): MutableList<Module> {
        return getGameModules().toMutableList().apply {
            remove(cacheModule)
            add(module {
                single(createdAtStart = true) {
                    cache
                }
            })
            add(module {
                single(override = true) {
                    mockk<InterfaceDecoder> {
                        every { get(any<Int>()) } answers { InterfaceDefinition(id = arg(0), components = (0..20).associateWith { InterfaceComponentDefinition(id = it) }) }
                    }
                }
            })
        }
    }

    override val modules = loadModules()

    override val propertyPaths = listOf("/test.properties")

    private lateinit var engine: GameLoop

    fun tick() {
        engine.run()
    }

    fun createPlayer(name: String, tile: Tile = Tile.EMPTY): Player {
        val loginQueue: LoginQueue = get()
        val factory: PlayerFactory = get()
        val index = loginQueue.login(name)!!
        val player = Player(id = -1, tile = tile, name = name, passwordHash = "")
        factory.initPlayer(player, index)
            tick()
            player.login()
            tick()
            player.viewport.loaded = true
        return player
    }

    fun Player.interfaceOption(name: String, component: String, option: String, item: Item = Item("", -1), slot: Int = -1) {
        val def = definitions.get(name)
        val comp = def.getComponentOrNull(component) ?: return
        val id = def.getComponentId(component) ?: -1
        val options = comp["options", emptyArray<String>()]
        events.emit(InterfaceOption(definitions.getId(name), name, id, component, options.indexOf(option), option, item, slot))
    }

    fun Player.playerOption(player: Player, option: String) {
        events.emit(PlayerOption(player, option, player.options.indexOf(option)))
    }

    @BeforeEach
    open fun setup() {
        val millis = measureTimeMillis {
            val tickStages = getTickStages(get(), get(), get(), get(), get(), get(), get())
            engine = GameLoop(mockk(relaxed = true), tickStages)
            get<EventHandlerStore>().populate(World)
            World.events.emit(Startup)
        }
        logger.info { "World startup took ${millis}ms" }
        definitions = get()
    }
}