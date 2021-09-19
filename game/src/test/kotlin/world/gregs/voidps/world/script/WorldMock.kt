package world.gregs.voidps.world.script

import com.github.michaelbull.logging.InlineLogger
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.getGameModules
import world.gregs.voidps.getTickStages
import world.gregs.voidps.utility.get
import kotlin.system.measureTimeMillis

abstract class WorldMock : KoinMock() {

    private val logger = InlineLogger()
    val cache: Cache = mockk(relaxed = true)

    init {
        every { cache.getFile(any(), archive = any(), file = any()) } returns null
        every { cache.getFile(Indices.ENUMS, archive = any(), file = any()) } returns byteArrayOf(5, 0, 0, 0)
    }

    override val modules = getGameModules().toMutableList().apply {
        remove(cacheModule)
        add(module {
            single(createdAtStart = true) {
                cache
            }
        })
    }

    override val propertyPaths = listOf("/test.properties")

    private lateinit var engine: GameLoop

    fun tick() {
        engine.run()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> loadScript(name: String): T {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.$name"
        return Class.forName(scriptPackage).constructors.first().newInstance(emptyArray<String>()) as T
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
    }
}