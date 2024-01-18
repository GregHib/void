package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.LoginServer
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.loadScripts
import java.io.File
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    lateinit var name: String
    private val logger = InlineLogger()

    @OptIn(ExperimentalUnsignedTypes::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        val properties = properties("/game.properties")
        name = properties.getProperty("name")

        val cache = Cache.load(properties)
        preload(cache, properties)

        val accountLoader = PlayerAccountLoader(get<ConnectionQueue>(), get(), Contexts.Game)
        val protocol = protocol(get<Huffman>())

        val gatekeeper: ConnectionGatekeeper = get()
        val loginServer = LoginServer.load(properties, protocol, gatekeeper, accountLoader, Contexts.Game)
        val server = GameServer.load(cache, properties, gatekeeper, loginServer)

        val tickStages = getTickStages(iterator = if (CharacterTask.DEBUG) SequentialIterator() else ParallelIterator())
        val engine = GameLoop(tickStages)
        World.start(properties)
        engine.start()

        logger.info { "$name loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload(cache: Cache, properties: Properties) {
        val module = cache(cache, properties)
        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            modules(engineModule, gameModule, module)
        }
        val saves = File(getProperty("savePath"))
        if (!saves.exists()) {
            saves.mkdir()
        }
        loadScripts(getProperty("scriptModule"))
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
        single(createdAtStart = true) { AmmoDefinitions().load() }
        single(createdAtStart = true) { ParameterDefinitions(CategoryDefinitions().load(), get()).load() }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().load(cache)).load() }
    }
}