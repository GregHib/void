package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveCache
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.loadScripts
import java.io.File
import java.math.BigInteger

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    lateinit var name: String
    private val logger = InlineLogger()
    private const val USE_ACTIVE_CACHE = true

    @OptIn(ExperimentalUnsignedTypes::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        val module = if (USE_ACTIVE_CACHE) {
            val activeDir = File("./data/cache/active/")
            ActiveCache().checkChanges(activeDir.parent, activeDir.name)
            active(activeDir)
        } else {
            cache(CacheDelegate("./data/cache/"))
        }
        preload(module)
        name = getProperty("name")
        val revision = getProperty("revision").toInt()
        val limit = getProperty("loginLimit").toInt()
        val modulus = BigInteger(getProperty("rsaModulus"), 16)
        val private = BigInteger(getProperty("rsaPrivate"), 16)

        val huffman: Huffman = get()
        val players: Players = get()
        val accounts: PlayerAccounts = get()
        val queue: ConnectionQueue = get()
        val gatekeeper: ConnectionGatekeeper = get()

        val accountLoader = PlayerAccountLoader(queue, accounts, Contexts.Game)
        val protocol = protocol(huffman)
        val server = Network(revision, modulus, private, gatekeeper, accountLoader, limit, Contexts.Game, protocol)

        val interfaceDefinitions: InterfaceDefinitions = get()
        val npcs: NPCs = get()
        val items: FloorItems = get()
        val objectDefinitions: ObjectDefinitions = get()

        val handler = InterfaceHandler(get(), interfaceDefinitions, get())
        val tickStages = getTickStages(
            players,
            npcs,
            items,
            get(),
            get(),
            queue,
            get(),
            get(),
            get(),
            objectDefinitions,
            get(),
            interfaceDefinitions,
            get(),
            handler,
            if (CharacterTask.DEBUG) SequentialIterator() else ParallelIterator())
        val engine = GameLoop(tickStages)

        World.start(getProperty("members") == "true")
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload(module: Module) {
        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
            modules(engineModule, gameModule, module)
        }
        val saves = File(getProperty("savePath"))
        if (!saves.exists()) {
            saves.mkdir()
        }
        loadScripts(getProperty("scriptModule"))
    }

    private fun active(activeDir: File) = module {
        single(createdAtStart = true) { MapDefinitions(get(), get(), get()).load(activeDir) }
        single(createdAtStart = true) { Huffman().load(activeDir.resolve(ActiveCache.indexFile(Index.HUFFMAN)).readBytes()) }
        single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(member = getProperty<String>("members") == "true", lowDetail = false, get<ParameterDefinitions>()).load(activeDir)).load() }
        single(createdAtStart = true) { NPCDefinitions(NPCDecoder(member = getProperty<String>("members") == "true", get<ParameterDefinitions>()).load(activeDir)).load() }
        single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get<ParameterDefinitions>()).load(activeDir)).load() }
        single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder().load(activeDir)).load() }
        single(createdAtStart = true) { EnumDefinitions(EnumDecoder().load(activeDir), get()).load() }
        single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder().load(activeDir)).load() }
        single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder().load(activeDir)).load() }
        single(createdAtStart = true) { InventoryDefinitions(InventoryDecoder().load(activeDir)).load() }
        single(createdAtStart = true) { StructDefinitions(StructDecoder(get<ParameterDefinitions>()).load(activeDir)).load() }
        single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().load(activeDir)).load() }
        single(createdAtStart = true) { WeaponStyleDefinitions().load() }
        single(createdAtStart = true) { AmmoDefinitions().load() }
        single(createdAtStart = true) { ParameterDefinitions(CategoryDefinitions().load(), get()).load() }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().load(activeDir)).load() }
    }

    private fun cache(cache: Cache) = module {
        single(createdAtStart = true) { MapDefinitions(get(), get(), get()).loadCache(cache, get<Xteas>()) }
        single(createdAtStart = true) { Huffman().load(cache.getFile(Index.HUFFMAN, 1)!!) }
        single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(member = getProperty<String>("members") == "true", lowDetail = false, get<ParameterDefinitions>()).loadCache(cache)).load() }
        single(createdAtStart = true) { NPCDefinitions(NPCDecoder(member = getProperty<String>("members") == "true", get<ParameterDefinitions>()).loadCache(cache)).load() }
        single(createdAtStart = true) { ItemDefinitions(ItemDecoder(get<ParameterDefinitions>()).loadCache(cache)).load() }
        single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder().loadCache(cache)).load() }
        single(createdAtStart = true) { EnumDefinitions(EnumDecoder().loadCache(cache), get()).load() }
        single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder().loadCache(cache)).load() }
        single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder().loadCache(cache)).load() }
        single(createdAtStart = true) { InventoryDefinitions(InventoryDecoder().loadCache(cache)).load() }
        single(createdAtStart = true) { StructDefinitions(StructDecoder(get<ParameterDefinitions>()).loadCache(cache)).load() }
        single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder().loadCache(cache)).load() }
        single(createdAtStart = true) { WeaponStyleDefinitions().load() }
        single(createdAtStart = true) { AmmoDefinitions().load() }
        single(createdAtStart = true) { ParameterDefinitions(CategoryDefinitions().load(), get()).load() }
        single(createdAtStart = true) { FontDefinitions(FontDecoder().loadCache(cache)).load() }
    }
}