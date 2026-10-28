package world.gregs.voidps.cache.serialization

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enum values by key, remembering whether they were in the order they were configured in.
 */
object EnumMapSerializer : KSerializer<Map<Int, Any>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.collections.Map<kotlin.Int, kotlin.Any>", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Map<Int, Any>) {
        encoder as AnyValueEncoder
        encoder.encodeInt(value.size)
        // Config defined enums keep their order
        encoder.encodeBoolean(value is LinkedHashMap<*, *>)
        for ((key, element) in value) {
            encoder.encodeInt(key)
            encoder.encodeAnyValue(element)
        }
    }

    override fun deserialize(decoder: Decoder): Map<Int, Any> {
        decoder as AnyValueDecoder
        val size = decoder.decodeInt()
        val ordered = decoder.decodeBoolean()
        val map: MutableMap<Int, Any> = if (ordered) LinkedHashMap(size) else Int2ObjectOpenHashMap(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val key = decoder.decodeInt()
            map[key] = decoder.decodeAnyValue()!!
        }
        return map
    }
}
