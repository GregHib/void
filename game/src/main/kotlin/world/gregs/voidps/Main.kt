package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.map.file.Maps
import world.gregs.voidps.engine.map.spawn.loadItemSpawns
import world.gregs.voidps.engine.map.spawn.loadNpcSpawns
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
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
        val ref = WeakReference(CacheDelegate("./data/cache/"))
        var cache: Cache? = ref.get()//getProperty("cachePath"))
        preload(cache!!)

        name = getProperty("name")
        val revision = getProperty("revision").toInt()
        val limit = getProperty("loginLimit").toInt()
        val modulus = BigInteger(getProperty("rsaModulus"), 16)
        val private = BigInteger(getProperty("rsaPrivate"), 16)
        val compress = getProperty("compressMaps") == "true"
        val path = getProperty("mapPath")

        val accountLoader = PlayerAccountLoader(get<ConnectionQueue>(), get(), Contexts.Game)
        val protocol = protocol(get())
        val server = Network(revision, modulus, private, get<ConnectionGatekeeper>(), accountLoader, limit, Contexts.Game, protocol)

        val tickStages = getTickStages(get(), get(), get<ConnectionQueue>(), get(), get(), get(), get())
        val engine = GameLoop(tickStages)

        World.start()
        Maps(cache, get(), get(), get(), get(), get(), get()).load(compress, path)
        loadObjectSpawns(get(), get())
        loadNpcSpawns(get())
        loadItemSpawns(get())
        cache = null
        ref.clear()

        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload(cache: Cache) {


        val storage = FileStorage()
        val enumDecoder = WeakReference(EnumDecoder(cache))
        val enumDefinitions = EnumDefinitions(enumDecoder.get()!!).load()
        enumDecoder.clear()
        val phraseDecoder = WeakReference(QuickChatPhraseDecoder(cache))
        val quickChatPhraseDefinitions = QuickChatPhraseDefinitions(phraseDecoder.get()!!).load()
        phraseDecoder.clear()

        val structDecoder = WeakReference(StructDecoder(cache))
        val structDefinitions = StructDefinitions(structDecoder.get()!!).load()
        structDecoder.clear()

        val objectDecoder = WeakReference(ObjectDecoder(cache, member = true, lowDetail = false, configReplace = true))
        val objectDefinitions = ObjectDefinitions(objectDecoder.get()!!).load(storage, "./data/definitions/objects.yml")
        objectDecoder.clear()

        val npcDecoder = WeakReference(NPCDecoder(cache, member = true))
        val nPCDefinitions = NPCDefinitions(npcDecoder.get()!!).load(storage, "./data/definitions/npcs.yml")
        npcDecoder.clear()

        val itemDecoder = WeakReference(ItemDecoder(cache))
        val itemDefinitions = ItemDefinitions(itemDecoder.get()!!).load(storage, "./data/definitions/items.yml")
        itemDecoder.clear()

        val animationDecoder = WeakReference(AnimationDecoder(cache))
        val animationDefinitions = AnimationDefinitions(animationDecoder.get()!!).load(storage, "./data/definitions/animations.yml")
        animationDecoder.clear()

        val graphicDecoder = WeakReference(GraphicDecoder(cache))
        val graphicDefinitions = GraphicDefinitions(graphicDecoder.get()!!).load(storage, "./data/definitions/graphics.yml")
        graphicDecoder.clear()

        val containerDecoder = WeakReference(ContainerDecoder(cache))
        val containerDefinitions = ContainerDefinitions(containerDecoder.get()!!).load(storage, "./data/definitions/containers.yml")
        containerDecoder.clear()

        val interfaceDecoder = WeakReference(InterfaceDecoder(cache))
        val interfaceDefinitions = InterfaceDefinitions(interfaceDecoder.get()!!).load(storage, "./data/definitions/interfaces.yml", "./data/definitions/interface-types.yml")
        interfaceDecoder.clear()

        val soundDefinitions = SoundDefinitions().load(storage, "./data/definitions/sounds.yml")
        val midiDefinitions = MidiDefinitions().load(storage, "./data/definitions/midis.yml")
        val variableDefinitions = VariableDefinitions().load(storage, "./data/definitions/variables.yml")
        val jingleDefinitions = JingleDefinitions().load(storage, "./data/definitions/jingles.yml")
        val spellDefinitions = SpellDefinitions().load(storage, "./data/definitions/spells.yml")
        val gearDefinitions = GearDefinitions().load(storage, "./data/definitions/gear-sets.yml")
        val itemOnItemDefinitions = ItemOnItemDefinitions().load(storage, "./data/definitions/item-on-item.yml")
        val scriptDecoder = WeakReference(ClientScriptDecoder(cache, revision634 = true))
        val styleDefinitions = StyleDefinitions().load(scriptDecoder.get()!!)
        scriptDecoder.clear()
        val accountDefinitions = AccountDefinitions(variableDefinitions).load(storage, "./data/saves/")
        val huffman = Huffman(cache)


        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
            val definitionsModule = module {
                single { huffman }
                single(createdAtStart = true) { enumDefinitions }
                single(createdAtStart = true) { quickChatPhraseDefinitions }
                single(createdAtStart = true) { structDefinitions }
                single(createdAtStart = true) { objectDefinitions }
                single(createdAtStart = true) { nPCDefinitions }
                single(createdAtStart = true) { itemDefinitions }
                single(createdAtStart = true) { animationDefinitions }
                single(createdAtStart = true) { graphicDefinitions }
                single(createdAtStart = true) { containerDefinitions }
                single(createdAtStart = true) { interfaceDefinitions }
                single(createdAtStart = true) { soundDefinitions }
                single(createdAtStart = true) { midiDefinitions }
                single(createdAtStart = true) { variableDefinitions }
                single(createdAtStart = true) { jingleDefinitions }
                single(createdAtStart = true) { spellDefinitions }
                single(createdAtStart = true) { gearDefinitions }
                single(createdAtStart = true) { itemOnItemDefinitions }
                single(createdAtStart = true) { styleDefinitions }
                single(createdAtStart = true) { accountDefinitions }
            }
            modules(getGameModules(definitionsModule))
        }
    }
}