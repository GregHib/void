import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.LoaderGraph
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.RegistrationServer
import world.gregs.voidps.network.login.protocol.decoders
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.web.WebServer
import world.gregs.voidps.web.account.AccountService
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.exists
import kotlin.system.exitProcess

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    private val logger = InlineLogger()
    private const val MIN_API_KEY_LENGTH = 32
    lateinit var server: GameServer
        private set

    private lateinit var graph: LoaderGraph
    private lateinit var loaded: Map<Loader<*>, Any>
    private lateinit var cache: Cache

    fun reload(name: String, files: ConfigFiles): List<String> {
        val (reloaded, results) = graph.reload(name, files, cache, loaded)
        loaded = results
        return reloaded
    }

    @JvmStatic
    fun main(args: Array<String>) {
        AuditLog.info("startup")
        val startTime = System.currentTimeMillis()
        ContentLoader.preload()
        val settings = settings()
        val files = CoroutineScope(Dispatchers.IO).async { configFiles() }

        // File server
        val cache = timed("cache") { Cache.load(settings) }
        server = GameServer.load(cache, settings)
        val port = Settings["network.port"].toInt()
        val job = server.start(port)
        AuditLog.info("login online")

        // Content
        val configFiles = runBlocking { files.await() }
        try {
            preload(cache, configFiles)
        } catch (ex: Exception) {
            logger.error(ex) { "Error loading files." }
            server.stop()
            exitProcess(1)
        }

        // Web server
        var site: Job? = null
        if (Settings["web.server.enabled", false]) {
            site = webServer(port, cache)
            if (site == null) {
                server.stop()
                return
            }
            AuditLog.info("web online")
        }

        // Login server
        val decoders = decoders(get<Huffman>())
        val accountLoader: PlayerAccountLoader = get()
        val loginServer = LoginServer.load(settings, decoders, accountLoader)
        val registrationServer = RegistrationServer.load(settings, accountLoader)

        // Game world
        val stages = getTickStages()
        World.start(configFiles)
        val scope = CoroutineScope(Contexts.Game)
        val engine = GameLoop(stages).start(scope)
        server.loginServer = loginServer
        server.registrationServer = registrationServer
        logger.info { "${Settings["server.name"]} loaded in ${System.currentTimeMillis() - startTime}ms" }
        AuditLog.info("game online")
        runBlocking {
            try {
                job.join()
            } finally {
                engine.cancel()
                server.stop()
                site?.cancel()
                AuditLog.info("game offline")
            }
        }
    }

    private fun settings(): Properties = timed("properties") {
        val properties = Settings.load()
        properties.putAll(System.getenv())
        return@timed properties
    }

    private fun preload(cache: Cache, configFiles: ConfigFiles) {
        startKoin {
            slf4jLogger(level = Level.ERROR)
        }
        this.cache = cache
        graph = LoaderGraph(engineLoaders() + gameLoaders())
        loaded = graph.run(configFiles, cache)
        val modules = listOf(
            engineModule(),
            gameModule(configFiles),
            definitionsModule(),
            gameDefinitionsModule(),
            module {
                single(createdAtStart = true) { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
                single { loaded.getValue(MapLoader) as MapDefinitions }
                single { cache }
            },
        )
        GlobalContext.loadKoinModules(modules, createEagerInstances = false)
        createEagerInstances(modules)
        engineLoad()
        Wildcards.load(Settings["storage.wildcards"])
        ContentLoader().load()
        Wildcards.update(Settings["storage.wildcards"])
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                World.shutdown()
                AuditLog.save()
            },
        )
    }

    /**
     * Creates all [modules] `createdAtStart` singletons in parallel.
     * Dependencies must be requested with `get()` so koin can block until they're created.
     */
    private fun createEagerInstances(modules: List<Module>) = runBlocking(Dispatchers.Default) {
        val koin = GlobalContext.get()
        modules
            .flatMap { it.eagerInstances }
            .map { factory -> async { koin.get<Any>(factory.beanDefinition.primaryType, factory.beanDefinition.qualifier) } }
            .awaitAll()
    }

    @Suppress("HttpUrlsUsage")
    private fun webServer(port: Int, cache: Cache): Job? {
        val path = Paths.get(Settings["web.client.path", ""])
        val webclient = if (path.exists()) {
            path
        } else {
            logger.warn { "No webclient file found at path: $path; webclient disabled." }
            null
        }
        val apiKey = if (Settings["web.api.enabled", false]) Settings["web.api.token", ""] else null
        if (apiKey != null && apiKey.length < MIN_API_KEY_LENGTH) {
            logger.error { "web.api.token must be at least $MIN_API_KEY_LENGTH characters when web.api.enabled is true, generate one with `openssl rand -hex 32`." }
            return null
        }
        val accounts = apiKey?.let { accountService() }
        val webPort = Settings["web.server.port"].toInt()
        val address = "localhost"
        val webServer = WebServer(webclient, webPort, address, port, get<Storage>(), get<QuestDefinitions>(), cache, apiKey, accounts)
        val scope = CoroutineScope(Dispatchers.IO)
        return scope.launch {
            logger.info { "Webserver online at http://$address:$webPort/" }
            webServer.start()
        }
    }

    private fun accountService(): AccountService {
        val limit = Settings["web.api.registration.maxPerIP", 3]
        val minutes = Settings["web.api.registration.windowMinutes", 60L]
        return AccountService(get(), get(), get(), RegistrationLimiter(limit, minutes * 60_000L))
    }
}
