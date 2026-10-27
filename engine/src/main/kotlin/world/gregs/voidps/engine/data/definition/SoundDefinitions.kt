package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.load.SnapshotCodec
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter

/**
 * Officially known as Synths
 */
class SoundDefinitions : DefinitionsDecoder<SoundDefinition> {

    override lateinit var definitions: Array<SoundDefinition>
    override lateinit var ids: Map<String, Int>

    fun install(state: DefinitionsState<SoundDefinition>) {
        definitions = state.definitions
        ids = state.ids
    }

    fun load(paths: List<String>): SoundDefinitions {
        install(SoundLoader.parse(paths))
        return this
    }

    override fun empty() = SoundDefinition.EMPTY
}

object SoundLoader : Loader<DefinitionsState<SoundDefinition>> {
    val instance = SoundDefinitions()
    override val name = "sounds"
    override val files = listOf("definitions.sounds")
    override val codec = object : SnapshotCodec<DefinitionsState<SoundDefinition>> {
        override val schema = 1L

        override fun write(writer: SnapshotWriter, value: DefinitionsState<SoundDefinition>) = with(writer) {
            ids(value.ids)
            // Several names can share an id, in which case the definition has the last one's
            val unique = value.ids.values.distinct()
            int(unique.size)
            for (id in unique) {
                val definition = value.definitions[id]
                int(id)
                string(definition.stringId)
                boolean(definition.params != null)
            }
        }

        override fun read(reader: SnapshotReader): DefinitionsState<SoundDefinition> {
            val ids = reader.ids()
            val definitions = Array(10_000) { SoundDefinition.EMPTY }
            repeat(reader.int()) {
                val id = reader.int()
                val stringId = reader.string()
                definitions[id] = if (reader.boolean()) {
                    SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
                } else {
                    SoundDefinition(id = id, stringId = stringId)
                }
            }
            return DefinitionsState(definitions, ids)
        }
    }

    override fun load(context: LoadContext): DefinitionsState<SoundDefinition> = parse(context.paths(this))

    override fun install(state: DefinitionsState<SoundDefinition>) = instance.install(state)

    fun parse(paths: List<String>): DefinitionsState<SoundDefinition> {
        val definitions = Array(10_000) { SoundDefinition.EMPTY }
        val ids = Object2IntOpenHashMap<String>(250, Hash.VERY_FAST_LOAD_FACTOR)
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    require(!ids.containsKey(stringId)) { "Duplicate sound id found '$stringId' at $path." }
                    var id = 0
                    var area = false
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> id = int()
                            "area" -> area = boolean()
                            else -> throw IllegalArgumentException("Unknown sound key: $key")
                        }
                    }
                    ids[stringId] = id
                    if (area) {
                        definitions[id] = SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
                    } else {
                        definitions[id] = SoundDefinition(id = id, stringId = stringId)
                    }
                }
            }
        }
        return DefinitionsState(definitions, ids)
    }
}
