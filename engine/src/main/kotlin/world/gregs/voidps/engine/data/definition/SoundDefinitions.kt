package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.data.snapshot.StringIdsSerializer

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
    override val codec = SnapshotFormat.codec(SoundStateSerializer)

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

/**
 * Several names can share an id, in which case the definition has the last one's, and every other id holds the shared empty definition.
 * So only the definitions which exist are stored.
 */
private object SoundStateSerializer : KSerializer<DefinitionsState<SoundDefinition>> {
    private val ids = StringIdsSerializer(null)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.SoundState") {
        element("ids", ids.descriptor)
        element("id", Int.serializer().descriptor)
        element("stringId", String.serializer().descriptor)
        element("area", Boolean.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: DefinitionsState<SoundDefinition>) {
        ids.serialize(encoder, value.ids)
        val unique = value.ids.values.distinct()
        encoder.encodeInt(unique.size)
        for (id in unique) {
            val definition = value.definitions[id]
            encoder.encodeInt(id)
            encoder.encodeString(definition.stringId)
            encoder.encodeBoolean(definition.params != null)
        }
    }

    override fun deserialize(decoder: Decoder): DefinitionsState<SoundDefinition> {
        val ids = ids.deserialize(decoder)
        val definitions = Array(10_000) { SoundDefinition.EMPTY }
        repeat(decoder.decodeInt()) {
            val id = decoder.decodeInt()
            val stringId = decoder.decodeString()
            definitions[id] = if (decoder.decodeBoolean()) {
                SoundDefinition(id = id, stringId = stringId, params = mapOf(Params.AREA to true))
            } else {
                SoundDefinition(id = id, stringId = stringId)
            }
        }
        return DefinitionsState(definitions, ids)
    }
}
