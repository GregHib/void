package world.gregs.voidps.engine.data.snapshot.serializer

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * String ids by number, restored as the [Object2IntOpenHashMap] the config parsers build.
 * @param default return value for unknown ids.
 */
class StringIdsSerializer(private val default: Int?) : KSerializer<Map<String, Int>> {
    override val descriptor: SerialDescriptor = MapSerializer(String.serializer(), Int.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: Map<String, Int>) {
        val composite = encoder.beginCollection(descriptor, value.size)
        var index = 0
        for ((stringId, id) in value) {
            composite.encodeStringElement(descriptor, index++, stringId)
            composite.encodeIntElement(descriptor, index++, id)
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Map<String, Int> {
        val composite = decoder.beginStructure(descriptor)
        val size = composite.decodeCollectionSize(descriptor)
        val ids = Object2IntOpenHashMap<String>(size, Hash.VERY_FAST_LOAD_FACTOR)
        if (default != null) {
            ids.defaultReturnValue(default)
        }
        var index = 0
        repeat(size) {
            val stringId = composite.decodeStringElement(descriptor, index++)
            ids[stringId] = composite.decodeIntElement(descriptor, index++)
        }
        composite.endStructure(descriptor)
        return ids
    }
}
