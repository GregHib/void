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
 * Definition params: untyped values by param id.
 * Restored the same way the config parsers build them, as a lookup optimised [Int2ObjectOpenHashMap].
 */
object ParamsSerializer : KSerializer<Map<Int, Any>?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("world.gregs.voidps.cache.Params", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Map<Int, Any>?) {
        encoder as AnyValueEncoder
        if (value == null) {
            encoder.encodeInt(-1)
            return
        }
        encoder.encodeInt(value.size)
        for ((key, param) in value) {
            encoder.encodeInt(key)
            encoder.encodeAnyValue(param)
        }
    }

    override fun deserialize(decoder: Decoder): Map<Int, Any>? {
        decoder as AnyValueDecoder
        val size = decoder.decodeInt()
        if (size == -1) {
            return null
        }
        val params = Int2ObjectOpenHashMap<Any>(maxOf(size, 4), Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val key = decoder.decodeInt()
            params[key] = decoder.decodeAnyValue()!!
        }
        return params
    }
}
