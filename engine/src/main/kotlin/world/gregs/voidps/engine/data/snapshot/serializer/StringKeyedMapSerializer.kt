package world.gregs.voidps.engine.data.snapshot.serializer

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Maps by string, restored as the [Object2ObjectOpenHashMap] the config parsers build.
 * @param loadFactor of the restored map.
 */
class StringKeyedMapSerializer<V>(
    private val value: KSerializer<V>,
    private val loadFactor: Float = Hash.DEFAULT_LOAD_FACTOR,
) : KSerializer<Map<String, V>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.collections.Map<kotlin.String, ${value.descriptor.serialName}>") {
        element("value", value.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Map<String, V>) {
        encoder.encodeInt(value.size)
        for ((key, element) in value) {
            encoder.encodeString(key)
            this.value.serialize(encoder, element)
        }
    }

    override fun deserialize(decoder: Decoder): Map<String, V> {
        val size = decoder.decodeInt()
        val map = Object2ObjectOpenHashMap<String, V>(size, loadFactor)
        repeat(size) {
            val key = decoder.decodeString()
            map[key] = value.deserialize(decoder)
        }
        return map
    }
}
