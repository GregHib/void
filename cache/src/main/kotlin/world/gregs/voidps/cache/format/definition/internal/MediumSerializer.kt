package world.gregs.voidps.cache.format.definition.internal

/*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.GeneratedSerializer
import world.gregs.voidps.cache.format.definition.Medium

@PublishedApi
@ExperimentalSerializationApi
@ExperimentalUnsignedTypes
@Serializer(forClass = Medium::class)
internal object MediumSerializer : KSerializer<Medium> {
    override val descriptor: SerialDescriptor = Int.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Medium) {
        encoder.encodeInline(descriptor).apply {
            encodeByte((value.value shr 16).toByte())
            encodeByte((value.value shr 8).toByte())
            encodeByte((value.value).toByte())
        }
    }

    override fun deserialize(decoder: Decoder): Medium {
        return (decoder.decodeInline(descriptor) as DefinitionDecoder).decodeMedium()
    }
}*/
