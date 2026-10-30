package world.gregs.voidps.cache.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Arrays of values of any type, in the tagged form [AnyValueEncoder] writes them.
 */
object AnyArraySerializer : KSerializer<Array<Any>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.Array<kotlin.Any>", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Array<Any>) {
        encoder as AnyValueEncoder
        encoder.encodeInt(value.size)
        for (element in value) {
            encoder.encodeAnyValue(element)
        }
    }

    override fun deserialize(decoder: Decoder): Array<Any> {
        decoder as AnyValueDecoder
        val size = decoder.decodeInt()
        return Array(size) { decoder.decodeAnyValue()!! }
    }
}
