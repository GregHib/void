package world.gregs.voidps.engine.data.snapshot

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
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

object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.ranges.IntRange", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeInt(value.first)
        encoder.encodeInt(value.last)
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val first = decoder.decodeInt()
        return first..decoder.decodeInt()
    }
}
