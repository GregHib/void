import com.github.michaelbull.logging.InlineLogger
import content.entity.obj.ObjectTeleports
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
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.login.protocol.decoders
import java.util.*
import kotlin.concurrent.thread

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
        val settings = settings()

        // File server
        val cache = timed("cache") { Cache.load(settings) }
        server = GameServer.load(cache, settings)
        val job = server.start(Settings["network.port"].toInt())
        AuditLog.info("login online")

        // Content
        val configFiles = configFiles()
        try {
            preload(cache, configFiles)
            if (configFiles.cacheUpdate || configFiles.extensions.isNotEmpty()) {
                updateModified()
            }
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
            modules(
                engineModule(configFiles),
                gameModule(configFiles),
                cache(cache, configFiles),
            )
        }
        Wildcards.load(Settings["storage.wildcards"])
        ContentLoader.load()
        Wildcards.update(Settings["storage.wildcards"])
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                Despawn.world()
                AuditLog.save()
            },
        )
    }

    private fun cache(cache: Cache, files: ConfigFiles) = module {
        val members = Settings["world.members", false]
        single(createdAtStart = true) { MapDefinitions(CollisionDecoder(get()), get(), get(), cache).loadCache() }
        single(createdAtStart = true) { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
        single(createdAtStart = true) {
            ObjectDefinitions(ObjectDecoder(members, lowDetail = false, get<ParameterDefinitions>()).load(cache)).load(files.list(Settings["definitions.objects"]))
        }
        single(createdAtStart = true) { NPCDefinitions(NPCDecoder(members, get<ParameterDefinitions>()).load(cache)).load(files.list(Settings["definitions.npcs"]), get(), get(), get()) }
        single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get<ParameterDefinitions>()).load(cache)).load(files.list(Settings["definitions.items"])) }
        single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder().load(cache)).load(files.list(Settings["definitions.animations"])) }
        single(createdAtStart = true) { EnumDefinitions(EnumDecoder().load(cache), get()).load(files.find(Settings["definitions.enums"])) }
        single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder().load(cache)).load(files.list(Settings["definitions.graphics"])) }
        single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder().load(cache)).load(files.list(Settings["definitions.interfaces"]), files.find(Settings["definitions.interfaces.types"])) }
        single(createdAtStart = true) { InventoryDefinitions(InventoryDecoder().load(cache)).load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"])) }
        single(createdAtStart = true) { StructDefinitions(StructDecoder(get<ParameterDefinitions>()).load(cache)).load(files.find(Settings["definitions.structs"])) }
        single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load() }
        single(createdAtStart = true) { WeaponStyleDefinitions().load(files.find(Settings["definitions.weapons.styles"])) }
        single(createdAtStart = true) { WeaponAnimationDefinitions().load(files.find(Settings["definitions.weapons.animations"])) }
        single(createdAtStart = true) { AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"])) }
        single(createdAtStart = true) { ParameterDefinitions(get(), get()).load(files.find(Settings["definitions.parameters"])) }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().load(cache)).load(files.find(Settings["definitions.fonts"])) }
        single(createdAtStart = true) { ItemOnItemDefinitions().load(files.list(Settings["definitions.itemOnItem"])) }
        single(createdAtStart = true) {
            VariableDefinitions().load(
                files.list(Settings["definitions.variables.players"]),
                files.list(Settings["definitions.variables.bits"]),
                files.list(Settings["definitions.variables.clients"]),
                files.list(Settings["definitions.variables.strings"]),
                files.list(Settings["definitions.variables.customs"]),
            )
        }
        single(createdAtStart = true) { DropTables().load(files.list(Settings["spawns.drops"]), get()) }
        single(createdAtStart = true) { ObjectTeleports().load(files.list(Settings["map.teleports"])) }
    }
}
