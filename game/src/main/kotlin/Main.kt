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
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.login.protocol.decoders
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    private val logger = InlineLogger()

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
        val configFiles = configFiles()
        try {
            preload(cache, configFiles)
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
        runBlocking {
            try {
                job.join()
            } finally {
                engine.cancel()
                server.stop()
            }
        }
    }

    private fun settings(): Properties = timed("properties") {
        val properties = Settings.load()
        properties.putAll(System.getenv())
        return@timed properties
    }

    private fun preload(cache: Cache, configFiles: Map<String, List<String>>) {
        val module = cache(cache, configFiles)
        startKoin {
            slf4jLogger(level = Level.ERROR)
            modules(engineModule, gameModule, module)
        }
        ContentLoader.load()
    }

    private fun cache(cache: Cache, files: Map<String, List<String>>) = module {
        val members = Settings["world.members", false]
        single(createdAtStart = true) { MapDefinitions(CollisionDecoder(get()), get(), get(), cache).loadCache() }
        single(createdAtStart = true) { Huffman().load(cache.data(Index.HUFFMAN, 1)!!) }
        single(createdAtStart = true) {
            ObjectDefinitions(ObjectDecoder(members, lowDetail = false, get<ParameterDefinitions>()).load(cache)).load(files.getOrDefault(Settings["definitions.objects"], emptyList()))
        }
        single(createdAtStart = true) { NPCDefinitions(NPCDecoder(members, get<ParameterDefinitions>()).load(cache)).load(files.getOrDefault(Settings["definitions.npcs"], emptyList())) }
        single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get<ParameterDefinitions>()).load(cache)).load(files.getOrDefault(Settings["definitions.items"], emptyList())) }
        single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder().load(cache)).load(files.getOrDefault(Settings["definitions.animations"], emptyList())) }
        single(createdAtStart = true) { EnumDefinitions(EnumDecoder().load(cache), get()).load() }
        single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder().load(cache)).load(files.getOrDefault(Settings["definitions.graphics"], emptyList())) }
        single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder().load(cache)).load(files.getOrDefault(Settings["definitions.interfaces"], emptyList())) }
        single(createdAtStart = true) { InventoryDefinitions(InventoryDecoder().load(cache)).load(files.getOrDefault(Settings["definitions.inventories"], emptyList())) }
        single(createdAtStart = true) { StructDefinitions(StructDecoder(get<ParameterDefinitions>()).load(cache)).load() }
        single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(cache)).load() }
        single(createdAtStart = true) { WeaponStyleDefinitions().load() }
        single(createdAtStart = true) { WeaponAnimationDefinitions().load() }
        single(createdAtStart = true) { AmmoDefinitions().load() }
        single(createdAtStart = true) { ParameterDefinitions(get(), get()).load() }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().load(cache)).load() }
        single(createdAtStart = true) { ItemOnItemDefinitions().load(files.getOrDefault(Settings["definitions.itemOnItem"], emptyList())) }
        single(createdAtStart = true) {
            VariableDefinitions().load(
                files.getOrDefault(Settings["definitions.variables.players"], emptyList()),
                files.getOrDefault(Settings["definitions.variables.bits"], emptyList()),
                files.getOrDefault(Settings["definitions.variables.clients"], emptyList()),
                files.getOrDefault(Settings["definitions.variables.strings"], emptyList()),
                files.getOrDefault(Settings["definitions.variables.customs"], emptyList()),
            )
        }
        single(createdAtStart = true) { DropTables().load(files.getOrDefault(Settings["spawns.drops"], emptyList()), get()) }
        single(createdAtStart = true) { ObjectTeleports().load(files.getOrDefault(Settings["map.teleports"], emptyList())) }
    }
}