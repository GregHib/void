package world.gregs.voidps.engine.data.snapshot.serializer

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Maps by int, restored as the [Int2ObjectOpenHashMap] the config parsers build.
 */
class IntKeyedMapSerializer<V>(
    private val value: KSerializer<V>,
    private val loadFactor: Float = Hash.VERY_FAST_LOAD_FACTOR,
) : KSerializer<Map<Int, V>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.collections.Map<kotlin.Int, ${value.descriptor.serialName}>") {
        element("value", value.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Map<Int, V>) {
        encoder.encodeInt(value.size)
        for ((key, element) in value) {
            encoder.encodeInt(key)
            this.value.serialize(encoder, element)
        }
    }

    override fun deserialize(decoder: Decoder): Map<Int, V> {
        val size = decoder.decodeInt()
        val map = Int2ObjectOpenHashMap<V>(size, loadFactor)
        repeat(size) {
            val key = decoder.decodeInt()
            map[key] = value.deserialize(decoder)
        }
        return map
    }
}
