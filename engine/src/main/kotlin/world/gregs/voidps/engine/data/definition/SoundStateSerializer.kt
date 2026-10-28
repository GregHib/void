package world.gregs.voidps.engine.data.definition

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.config.SoundDefinition
import world.gregs.voidps.engine.data.load.DefinitionsState
import world.gregs.voidps.engine.data.snapshot.serializer.StringIdsSerializer

/**
 * Several names can share an id, in which case the definition has the last one's, and every other id holds the shared empty definition.
 * So only the definitions which exist are stored.
 */
internal object SoundStateSerializer : KSerializer<DefinitionsState<SoundDefinition>> {
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
