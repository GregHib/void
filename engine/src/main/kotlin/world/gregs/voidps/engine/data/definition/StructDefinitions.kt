package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.data.load.DefinitionsCodec
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

/**
 * Also known as AttributeMaps in cs2 or rows
 */
object StructDefinitions : DefinitionsDecoder<StructDefinition> {

    override var definitions: Array<StructDefinition> = emptyArray()

    override var ids: Map<String, Int> = emptyMap()

    var loaded = false
        private set

    fun init(definitions: Array<StructDefinition>): StructDefinitions {
        this.definitions = definitions
        loaded = true
        return this
    }

    @TestOnly
    fun set(definitions: Array<StructDefinition>, ids: Map<String, Int>) {
        this.definitions = definitions
        this.ids = ids
        loaded = true
    }

    fun clear() {
        this.definitions = emptyArray()
        this.ids = emptyMap()
        loaded = false
    }

    override fun empty() = StructDefinition.EMPTY

    fun install(state: DefinitionsState<StructDefinition>) {
        definitions = state.definitions
        ids = state.ids
        loaded = true
    }

    /**
     * Applies the config at [path] to the already decoded [definitions].
     */
    fun load(path: String): StructDefinitions {
        ids = StructLoader.parse(definitions, path)
        return this
    }
}

object StructLoader : Loader<DefinitionsState<StructDefinition>> {
    override val name = "structs"
    override val file = listOf("definitions.structs")
    override val usesCache = true
    override val codec = DefinitionsCodec(
        StructDefinition::class.java,
        default = null,
        writeDefinition = { definition ->
            int(definition.id)
            string(definition.stringId)
            params(definition.params)
        },
        readDefinition = {
            StructDefinition(
                id = int(),
                stringId = string(),
                params = params(),
            )
        },
    )

    override fun load(context: LoadContext): DefinitionsState<StructDefinition> {
        val definitions = StructDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this).single()))
    }

    override fun install(state: DefinitionsState<StructDefinition>) = StructDefinitions.install(state)

    fun parse(definitions: Array<StructDefinition>, path: String): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>(512, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path) {
            while (nextPair()) {
                val stringId = key()
                val id = int()
                require(!ids.containsKey(stringId)) { "Duplicate struct id found '$stringId' at $path." }
                ids[stringId] = id
                definitions[id].stringId = stringId
            }
        }
        return ids
    }
}
