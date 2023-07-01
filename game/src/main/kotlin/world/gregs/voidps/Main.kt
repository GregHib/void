package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.loadCache
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.*
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.map.file.Maps
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.loadScripts
import world.gregs.voidps.world.activity.quest.bookModule
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.spawn.stairsModule
import java.io.File
import java.lang.ref.SoftReference
import java.math.BigInteger

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
        preload()

        name = getProperty("name")
        val revision = getProperty("revision").toInt()
        val limit = getProperty("loginLimit").toInt()
        val modulus = BigInteger(getProperty("rsaModulus"), 16)
        val private = BigInteger(getProperty("rsaPrivate"), 16)

        val huffman: Huffman = get()
        val players: Players = get()
        val factory: PlayerFactory = get()
        val queue: ConnectionQueue = get()
        val gatekeeper: ConnectionGatekeeper = get()

        val accountLoader = PlayerAccountLoader(queue, factory, Contexts.Game)
        val protocol = protocol(huffman)
        val server = Network(revision, modulus, private, gatekeeper, accountLoader, limit, Contexts.Game, protocol)

        val interfaceDefinitions: InterfaceDefinitions = get()
        val npcs: NPCs = get()
        val items: FloorItems = get()
        val objectDefinitions: ObjectDefinitions = get()

        val handler = InterfaceHandler(get(), interfaceDefinitions, get())
        val tickStages = getTickStages(players, npcs, items, get(), get(), queue, get(), get(), objectDefinitions, get(), interfaceDefinitions, handler, ParallelIterator())
        val engine = GameLoop(tickStages)

        World.start(getProperty("members") == "true")
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload() {
        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
            modules(engineModule, stairsModule, musicModule, bookModule, gameModule, postCacheModule, postCacheGameModule,
                module {
                    single(createdAtStart = true) { SoftReference(CacheDelegate(getProperty("cachePath")) as Cache) }
                    single(createdAtStart = true) {
                        val huffman = cache().getFile(Indices.HUFFMAN, 1)!!
                        Huffman(huffman)
                    }
                    single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(cache(), member = true, lowDetail = false)).load() }
                    single(createdAtStart = true) { NPCDefinitions(NPCDecoder(cache(), member = true)).load() }
                    single(createdAtStart = true) { ItemDefinitions(loadCache(cache(), ItemDecoder(cache()))).load() }
                    single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder(cache())).load() }
                    single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder(cache())).load() }
                    single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder(cache())).load() }
                    single(createdAtStart = true) { ContainerDefinitions(ContainerDecoder(cache())).load() }
                    single(createdAtStart = true) { StructDefinitions(StructDecoder(cache())).load() }
                    single(createdAtStart = true) { EnumDefinitions(EnumDecoder(cache()), get()).load() }
                    single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder(cache())).load() }
                    single(createdAtStart = true) { StyleDefinitions().load(ClientScriptDecoder(cache(), revision634 = true)) }
                    single(named("mapLoader"), createdAtStart = true) { Maps(cache(), get(), get(), get(), get(), get()).load() }
                })
        }
        val saves = File(getProperty("savePath"))
        if (!saves.exists()) {
            saves.mkdir()
        }
        loadKoinModules(listOf(postCacheModule, postCacheGameModule))
        loadScripts(getProperty("scriptModule"))
        val cacheRef = get<SoftReference<Cache>>()
        cacheRef.get()!!.close()
        cacheRef.clear()
    }

    private fun cache() = get<SoftReference<Cache>>().get()!!
}