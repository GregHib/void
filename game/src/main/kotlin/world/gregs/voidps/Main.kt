package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.bot.taskModule
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.gameModule
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.file.Maps
import world.gregs.voidps.engine.map.spawn.loadItemSpawns
import world.gregs.voidps.engine.map.spawn.loadNpcSpawns
import world.gregs.voidps.engine.postCacheModule
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.loadScripts
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.stairsModule
import java.io.File
import java.lang.ref.WeakReference
import java.math.BigInteger

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    lateinit var name: String
    private val logger = InlineLogger()

    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        preload()

        name = getProperty("name")
        val revision = getProperty("revision").toInt()
        val limit = getProperty("loginLimit").toInt()
        val modulus = BigInteger(getProperty("rsaModulus"), 16)
        val private = BigInteger(getProperty("rsaPrivate"), 16)

        val collisions: Collisions = get()
        val huffman: Huffman = get()
        val players: Players = get()
        val factory: PlayerFactory = get()
        val save: PlayerSave = get()
        val queue: ConnectionQueue = get()
        val gatekeeper: ConnectionGatekeeper = get()

        val accountLoader = PlayerAccountLoader(queue, factory, save, Contexts.Game, collisions, players)
        val protocol = protocol(huffman)
        val server = Network(revision, modulus, private, gatekeeper, accountLoader, limit, Contexts.Game, protocol)

        val interfaceDefinitions: InterfaceDefinitions = get()
        val npcs: NPCs = get()
        val items: FloorItems = get()
        val objectDefinitions: ObjectDefinitions = get()

        val handler = InterfaceHandler(get(), interfaceDefinitions, get())
        val tickStages = getTickStages(players, npcs, items, get(), queue, get(), get(), collisions, get(), objectDefinitions, get(), interfaceDefinitions, handler, ParallelIterator(), ParallelIterator())
        val engine = GameLoop(tickStages)

        World.start(getProperty("members") == "true")
        loadObjectSpawns(get(), objectDefinitions)
        loadNpcSpawns(npcs)
        loadItemSpawns(items)

        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload() {
        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
            modules(gameModule, stairsModule, musicModule, taskModule)
        }
        val saves = File(getProperty("savePath"))
        if (!saves.exists()) {
            saves.mkdir()
        }
        preloadCache()
    }

    private fun preloadCache() {
        val cache = WeakReference(CacheDelegate(getProperty("cachePath")) as Cache)
        val huffman = cache.get()!!.getFile(Indices.HUFFMAN, 1)!!
        val cacheRef = cache.get()!!
        loadKoinModules(module {
            single(createdAtStart = true) { Huffman(huffman) }
            single(createdAtStart = true) { ObjectDefinitions(ObjectDecoder(cacheRef, member = true, lowDetail = false)).load() }
            single(createdAtStart = true) { NPCDefinitions(NPCDecoder(cacheRef, member = true)).load() }
            single(createdAtStart = true) { ItemDefinitions(ItemDecoder(cacheRef)).load() }
            single(createdAtStart = true) { AnimationDefinitions(AnimationDecoder(cacheRef)).load() }
            single(createdAtStart = true) { GraphicDefinitions(GraphicDecoder(cacheRef)).load() }
            single(createdAtStart = true) { InterfaceDefinitions(InterfaceDecoder(cacheRef)).load() }
            single(createdAtStart = true) { ContainerDefinitions(ContainerDecoder(cacheRef)).load() }
            single(createdAtStart = true) { StructDefinitions(StructDecoder(cacheRef)).load() }
            single(createdAtStart = true) { EnumDefinitions(EnumDecoder(cacheRef), get()).load() }
            single(createdAtStart = true) { QuickChatPhraseDefinitions(QuickChatPhraseDecoder(cacheRef)).load() }
            single(createdAtStart = true) { StyleDefinitions().load(ClientScriptDecoder(cacheRef, revision634 = true)) }
        })
        loadKoinModules(postCacheModule)
        loadScripts(getProperty("scriptModule"))
        Maps(cache.get()!!, get(), get(), get(), get(), get(), get(), get()).load()
        cache.clear()
    }
}