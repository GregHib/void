package world.gregs.voidps.cache.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Arrays of nullable strings, read straight into an array without the intermediate list the generic array serializer uses.
 */
object NullableStringArraySerializer : KSerializer<Array<String?>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.Array<kotlin.String?>", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Array<String?>) {
        encoder.encodeInt(value.size)
        for (string in value) {
            if (string == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeNotNullMark()
                encoder.encodeString(string)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Array<String?> {
        val array = arrayOfNulls<String>(decoder.decodeInt())
        for (i in array.indices) {
            array[i] = if (decoder.decodeNotNullMark()) decoder.decodeString() else decoder.decodeNull()
        }
        return array
    }
}
