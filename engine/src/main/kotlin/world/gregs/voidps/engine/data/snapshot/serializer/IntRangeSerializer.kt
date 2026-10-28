package world.gregs.voidps.engine.data.snapshot.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
