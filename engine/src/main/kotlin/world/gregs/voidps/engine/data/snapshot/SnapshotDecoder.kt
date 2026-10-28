package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.cache.serialization.AnyValueDecoder

/**
 * Both the [Decoder] and its [CompositeDecoder] so every element is read with one call.
 */
@OptIn(ExperimentalSerializationApi::class)
class SnapshotDecoder(private val reader: SnapshotReader) :
    Decoder,
    CompositeDecoder,
    AnyValueDecoder {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun decodeSequentially() = true

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalStateException("Snapshots are only decoded sequentially.")

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = reader.int()

    override fun decodeNotNullMark() = reader.boolean()

    override fun decodeNull(): Nothing? = null

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = this

    override fun decodeBoolean() = reader.boolean()

    override fun decodeByte() = reader.byte().toByte()

    override fun decodeShort() = reader.short().toShort()

    override fun decodeChar() = reader.int().toChar()

    override fun decodeInt() = reader.int()

    override fun decodeLong() = reader.long()

    override fun decodeFloat() = java.lang.Float.intBitsToFloat(reader.int())

    override fun decodeDouble() = reader.double()

    override fun decodeString() = reader.string()

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = reader.int()

    override fun decodeAnyValue(): Any? = reader.value()

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int) = reader.boolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int) = reader.byte().toByte()

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int) = reader.short().toShort()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int) = reader.int().toChar()

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int) = reader.int()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int) = reader.long()

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int) = java.lang.Float.intBitsToFloat(reader.int())

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int) = reader.double()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int) = reader.string()

    override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T = deserializer.deserialize(this)

    override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? = if (deserializer.descriptor.isNullable || decodeNotNullMark()) {
        deserializer.deserialize(this)
    } else {
        decodeNull()
    }
}
