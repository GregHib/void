package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.cache.definition.decoder.FontDecoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.DefinitionsStateSerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

class FontDefinitions(
    override var definitions: Array<FontDefinition>,
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun install(state: DefinitionsState<FontDefinition>) {
        definitions = state.definitions
        ids = state.ids
    }

    /**
     * Applies the config at [path] to the already decoded [definitions].
     */
    fun load(path: String): FontDefinitions {
        ids = FontLoader.parse(definitions, path)
        return this
    }
}

object FontLoader : Loader<DefinitionsState<FontDefinition>> {
    val instance = FontDefinitions(emptyArray())
    override val name = "fonts"
    override val file = listOf("definitions.fonts")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(DefinitionsStateSerializer(FontDefinition::class, FontDefinition.serializer(), default = null))

    override fun load(context: LoadContext): DefinitionsState<FontDefinition> {
        val definitions = FontDecoder().load(context.requireCache())
        return DefinitionsState(definitions, parse(definitions, context.paths(this).single()))
    }

    override fun install(state: DefinitionsState<FontDefinition>) = instance.install(state)

    fun parse(definitions: Array<FontDefinition>, path: String): Map<String, Int> {
        val ids = Object2IntOpenHashMap<String>(20, Hash.VERY_FAST_LOAD_FACTOR)
        Config.fileReader(path) {
            while (nextPair()) {
                val key = key()
                val id = int()
                ids[key] = id
                definitions[id].stringId = key
            }
        }
        return ids
    }
}
