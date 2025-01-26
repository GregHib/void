package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.login.protocol.decoders
import world.gregs.voidps.script.loadScripts
import java.util.*

/**
 * Entry point for the application.
 *
 * The `Main` object initializes and starts various subsystems required for the application such as
 * file server, content loader, login server, and game world. Additionally, it handles server shutdown
 * and cleanup processes to ensure graceful termination of all resources.
 *
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    /**
     * A logger instance used for logging information, warnings, and errors throughout the application.
     * This logger is based on inline logging, optimizing performance where logging evaluation can be skipped.
     *
     * Commonly utilized for crucial application events such as errors during application startup,
     * processing game logic, or various system operations.
     */
    private val logger = InlineLogger()

    /**
     * Entry point of the application.
     * This method initializes and starts various components required for the server, including:
     * - Loading cache and settings
     * - Configuring and starting the game server
     * - Preloading content
     * - Setting up the login server
     * - Starting the game world and its loop
     * - Handling server shutdown gracefully
     *
     * @param args Command-line arguments passed to the application.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        val settings = settings()

        // File server
        val cache = timed("cache") { Cache.load(settings) }
        val server = GameServer.load(cache, settings)
        val job = server.start(Settings["network.port"].toInt())

        // Content
        try {
            preload(cache)
        } catch (ex: Exception) {
            logger.error(ex) { "Error loading files." }
            server.stop()
        }

        // Login server
        val decoders = decoders(get<Huffman>())
        val accountLoader: PlayerAccountLoader = get()
        val loginServer = LoginServer.load(settings, decoders, accountLoader)

        // Game world
        val stages = getTickStages()
        World.start()
        val scope = CoroutineScope(Contexts.Game)
        val engine = GameLoop(stages).start(scope)
        server.loginServer = loginServer
        logger.info { "${Settings["server.name"]} loaded in ${System.currentTimeMillis() - startTime}ms" }
        runBlocking {
            try {
                job.join()
            } finally {
                engine.cancel()
                server.stop()
            }
        }
    }

    /**
     * Loads and combines property settings from the specified property file and environment variables.
     * The method execution time is logged for performance monitoring.
     *
     * @return A Properties object containing the combined configuration settings from the file and system environment variables.
     */
    private fun settings(): Properties = timed("properties") {
        val properties = Settings.load()
        properties.putAll(System.getenv())
        return@timed properties
    }

    /**
     * Preloads the necessary modules, initializes the dependency injection container,
     * and loads scripts for the application.
     *
     * @param cache The cache instance used to initialize various modules and definitions needed for the application.
     */
    private fun preload(cache: Cache) {
        val module = cache(cache)
        startKoin {
            slf4jLogger(level = Level.ERROR)
            modules(engineModule, gameModule, module)
        }
        loadScripts()
    }

    /**
     * Configures dependencies and initializes cache-related definitions and decoders.
     *
     * @param cache an instance of the Cache interface, representing the game cache being processed and used for loading various game definitions.
     */
    private fun cache(cache: Cache) = module {
        val members = Settings["world.members", false]
        single(createdAtStart = true) { MapDefinitions(CollisionDecoder(get()), get(), get(), cache).loadCache() }
        single(createdAtStart = true) { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
        single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(members, lowDetail = false, get<ParameterDefinitions>()).load(cache)).load() }
        single(createdAtStart = true) { NPCDefinitions(NPCDecoder(members, get<ParameterDefinitions>()).load(cache)).load() }
        single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get<ParameterDefinitions>()).load(cache)).load() }
        single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder().load(cache)).load() }
        single(createdAtStart = true) { EnumDefinitions(EnumDecoder().load(cache), get()).load() }
        single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder().load(cache)).load() }
        single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder().load(cache)).load() }
        single(createdAtStart = true) { InventoryDefinitions(InventoryDecoder().load(cache)).load() }
        single(createdAtStart = true) { StructDefinitions(StructDecoder(get<ParameterDefinitions>()).load(cache)).load() }
        single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load() }
        single(createdAtStart = true) { WeaponStyleDefinitions().load() }
        single(createdAtStart = true) { WeaponAnimationDefinitions().load() }
        single(createdAtStart = true) { AmmoDefinitions().load() }
        single(createdAtStart = true) { ParameterDefinitions(CategoryDefinitions().load(), get()).load() }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().load(cache)).load() }
    }
}