import com.github.michaelbull.logging.InlineLogger
import content.entity.obj.ObjectTeleports
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
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.data.snapshot.restoreOrLoad
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.login.protocol.decoders
import world.gregs.voidps.web.WebServer
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
    lateinit var server: GameServer
        private set

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

        // Game world
        val stages = getTickStages()
        World.start(configFiles)
        val scope = CoroutineScope(Contexts.Game)
        val engine = GameLoop(stages).start(scope)
        server.loginServer = loginServer
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
        configFiles.update()
        startKoin {
            slf4jLogger(level = Level.ERROR)
        }
        val modules = listOf(
            engineModule(configFiles),
            gameModule(configFiles),
            cache(cache, configFiles),
        )
        GlobalContext.loadKoinModules(modules, createEagerInstances = false)
        createEagerInstances(modules)
        engineLoad(configFiles)
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

    /**
     * A snapshot of the definitions themselves, if enabled.
     */
    private fun ConfigFiles.full(name: String, paths: List<String>, dependencies: List<String> = emptyList()): Snapshot? = if (Settings["storage.snapshots.full", true]) snapshot(name, paths, dependencies) else null

    private fun cache(cache: Cache, files: ConfigFiles): Module {
        val members = Settings["world.members", false]
        // Whether to snapshot the definitions themselves, instead of only the parsed configs
        val fullSnapshots = Settings["storage.snapshots.full", true]
        val module = module {
            single(createdAtStart = true) {
                get<ObjectDefinitions>()
                MapDefinitions(CollisionDecoder(), cache).load(files)
            }
            single(createdAtStart = true) { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.objects"])
                val all = if (fullSnapshots) files.snapshot("objects.all", paths) else null
                if (all == null || !ObjectDefinitions.restoreDefinitions(all)) {
                    ObjectDefinitions.init(ObjectDecoder(members, lowDetail = false).load(cache)).load(paths, if (all == null) files.snapshot("objects", paths) else null)
                    all?.let { ObjectDefinitions.saveDefinitions(it) }
                }
                ObjectDefinitions
            }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.npcs"])
                val all = if (fullSnapshots) files.snapshot("npcs.all", paths) else null
                if (all == null || !NPCDefinitions.restoreDefinitions(all)) {
                    NPCDefinitions.init(NPCDecoder(members).load(cache)).load(paths, get<DropTables>(), if (all == null) files.snapshot("npcs", paths) else null)
                    all?.let { NPCDefinitions.saveDefinitions(it) }
                }
                NPCDefinitions
            }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.items"])
                val all = if (fullSnapshots) files.snapshot("items.all", paths) else null
                if (all == null || !ItemDefinitions.restoreDefinitions(all)) {
                    ItemDefinitions.init(ItemDecoder().load(cache)).load(paths, if (all == null) files.snapshot("items", paths) else null)
                    all?.let { ItemDefinitions.saveDefinitions(it) }
                }
                ItemDefinitions
            }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.animations"])
                files.full("animations", paths).restoreOrLoad(AnimationDefinitions::restoreDefinitions, AnimationDefinitions::saveDefinitions) {
                    AnimationDefinitions.init(AnimationDecoder().load(cache)).load(paths)
                }
                AnimationDefinitions
            }
            single(createdAtStart = true) {
                get<AnimationDefinitions>()
                get<GraphicDefinitions>()
                get<ItemDefinitions>()
                get<InterfaceDefinitions>()
                get<InventoryDefinitions>()
                get<NPCDefinitions>()
                get<StructDefinitions>()
                get<ObjectDefinitions>()
                get<Tables>()
                EnumDefinitions.init(EnumDecoder().load(cache)).load(files.list(Settings["definitions.enums"]))
            }
            single(createdAtStart = true) {
                get<AnimationDefinitions>()
                get<GraphicDefinitions>()
                get<ItemDefinitions>()
                get<NPCDefinitions>()
                get<ObjectDefinitions>()
                get<VariableDefinitions>()
                val paths = files.list(Settings["definitions.tables"])
                // Rows store the ids of the entities they reference
                val dependencies = listOf("definitions.items", "definitions.npcs", "definitions.objects", "definitions.animations", "definitions.graphics").flatMap { files.list(Settings[it]) }
                Tables.load(paths, files.snapshot("tables", paths, dependencies))
            }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.graphics"])
                files.full("graphics", paths).restoreOrLoad(GraphicDefinitions::restoreDefinitions, GraphicDefinitions::saveDefinitions) {
                    GraphicDefinitions.init(GraphicDecoder().load(cache)).load(paths)
                }
                GraphicDefinitions
            }
            single(createdAtStart = true) {
                val paths = files.list(Settings["definitions.interfaces"])
                val types = files.find(Settings["definitions.interfaces.types"])
                files.full("interfaces", paths + types).restoreOrLoad(InterfaceDefinitions::restoreDefinitions, InterfaceDefinitions::saveDefinitions) {
                    InterfaceDefinitions.init(InterfaceDecoder().load(cache)).load(paths, types)
                }
                InterfaceDefinitions
            }
            single(createdAtStart = true) {
                get<ItemDefinitions>()
                val paths = files.list(Settings["definitions.inventories"])
                val shops = files.list(Settings["definitions.shops"])
                // Inventories store the ids of the items they contain
                val items = files.list(Settings["definitions.items"])
                files.full("inventories", paths + shops, items).restoreOrLoad(InventoryDefinitions::restoreDefinitions, InventoryDefinitions::saveDefinitions) {
                    InventoryDefinitions.init(InventoryDecoder().load(cache)).load(paths, shops)
                }
                InventoryDefinitions
            }
            single(createdAtStart = true) {
                val path = files.find(Settings["definitions.structs"])
                files.full("structs", listOf(path)).restoreOrLoad(StructDefinitions::restoreDefinitions, StructDefinitions::saveDefinitions) {
                    StructDefinitions.init(StructDecoder().load(cache)).load(path)
                }
                StructDefinitions
            }
            single(createdAtStart = true) {
                val snapshot = files.full("quick_chat", emptyList())
                snapshot?.let { QuickChatPhraseDefinitions.restore(it) } ?: QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load().also { phrases -> snapshot?.let(phrases::save) }
            }
            single(createdAtStart = true) { WeaponStyleDefinitions().load(files.find(Settings["definitions.weapons.styles"])) }
            single(createdAtStart = true) { WeaponAnimationDefinitions().load(files.find(Settings["definitions.weapons.animations"])) }
            single(createdAtStart = true) { AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"])) }
            single(createdAtStart = true) {
                val path = files.find(Settings["definitions.fonts"])
                val snapshot = files.full("fonts", listOf(path))
                snapshot?.let { FontDefinitions.restore(it) } ?: FontDefinitions(FontDecoder().load(cache)).load(path).also { fonts -> snapshot?.let(fonts::save) }
            }
            single(createdAtStart = true) {
                get<ItemDefinitions>()
                ItemOnItemDefinitions().load(files.list(Settings["definitions.itemOnItem"]))
            }
            single(createdAtStart = true) {
                VariableDefinitions.load(files)
            }
            single(createdAtStart = true) {
                get<ItemDefinitions>()
                DropTables().load(files.list(Settings["spawns.drops"]))
            }
            single(createdAtStart = true) { ObjectTeleports().load(files.list(Settings["map.teleports"])) }
        }
        return module
    }

    @Suppress("HttpUrlsUsage")
    private fun webServer(port: Int, cache: Cache): Job? {
        val path = Paths.get(Settings["web.client.path", ""])
        if (!path.exists()) {
            logger.error { "No webclient file found at path: $path" }
            return null
        }
        val webPort = Settings["web.server.port"].toInt()
        val address = "localhost"
        val webServer = WebServer(path, webPort, address, port, get<Storage>(), get<QuestDefinitions>(), cache)
        val scope = CoroutineScope(Dispatchers.IO)
        return scope.launch {
            logger.info { "Webserver online at http://$address:$webPort/" }
            webServer.start()
        }
    }
}
