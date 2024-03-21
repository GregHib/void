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
import world.gregs.voidps.engine.client.ClientManager
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.LoginManager
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.loadScripts
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main : CoroutineScope {

    override val coroutineContext: CoroutineContext = Contexts.Game
    lateinit var name: String
    private val logger = InlineLogger()
    private const val PROPERTY_FILE_NAME = "game.properties"

    @OptIn(ExperimentalUnsignedTypes::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        val properties = properties()
        name = properties.getProperty("name")

        // File server
        val cache = timed("cache") { Cache.load(properties) }
        val server = GameServer.load(cache, properties, ClientManager())
        val job = server.start(properties.getProperty("port").toInt())

        // Content
        try {
            preload(cache, properties)
        } catch (ex: Exception) {
            logger.error(ex) { "Error loading files." }
            job.cancel()
        }

        // Login server
        val protocol = protocol(get<Huffman>())
        val accounts: LoginManager = get()
        val accountLoader = PlayerAccountLoader(get<ConnectionQueue>(), get(), Contexts.Game)
        val loginServer = LoginServer.load(properties, protocol, accounts, accountLoader)

        // Game world
        val stages = getTickStages()
        val engine = GameLoop(stages)
        World.start(properties)
        engine.start()
        server.loginServer = loginServer
        logger.info { "$name loaded in ${System.currentTimeMillis() - startTime}ms" }
        runBlocking {
            try {
                job.join()
            } finally {
                engine.stop()
            }
        }
    }

    private fun properties(): Properties = timed("properties") {
        val properties = Properties()
        val file = File("./$PROPERTY_FILE_NAME")
        if (file.exists()) {
            properties.load(file.inputStream())
        } else {
            logger.debug { "Property file not found; defaulting to internal." }
            properties.load(Main::class.java.getResourceAsStream("/$PROPERTY_FILE_NAME"))
        }
        return@timed properties
    }

    @Suppress("UNCHECKED_CAST")
    private fun preload(cache: Cache, properties: Properties) {
        val module = cache(cache, properties)
        startKoin {
            slf4jLogger(level = Level.ERROR)
            properties(properties.toMap() as Map<String, Any>)
            modules(engineModule, gameModule, module)
        }
        loadScripts()
    }

    private fun cache(cache: Cache, properties: Properties) = module {
        val members = properties.getProperty("members").toBoolean()
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