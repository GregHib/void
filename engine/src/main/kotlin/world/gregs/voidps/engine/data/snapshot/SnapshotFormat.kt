package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.cache.serialization.AnyValueDecoder
import world.gregs.voidps.cache.serialization.AnyValueEncoder
import world.gregs.voidps.engine.data.load.SnapshotCodec

/**
 * A sequential binary format: values are written in declaration order with no names or tags,
 * so what's read must have exactly the shape of what was written.
 * That's guaranteed by the schema fingerprint being part of every snapshot's key.
 */
@OptIn(ExperimentalSerializationApi::class)
object SnapshotFormat : BinaryFormat {

    /** Increment when the binary encoding rules change, not when the data changes. */
    const val FORMAT_VERSION = 1L

    override val serializersModule: SerializersModule = EmptySerializersModule()

    fun <T> encode(serializer: SerializationStrategy<T>, value: T, writer: SnapshotWriter) {
        SnapshotEncoder(writer).encodeSerializableValue(serializer, value)
    }

    fun <T> decode(deserializer: DeserializationStrategy<T>, reader: SnapshotReader): T = SnapshotDecoder(reader).decodeSerializableValue(deserializer)

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val writer = SnapshotWriter()
        encode(serializer, value, writer)
        return writer.toByteArray()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T = decode(deserializer, SnapshotReader(bytes))

    /**
     * Adapts a generated [serializer] to a [SnapshotCodec], with its schema derived from the shape of the serialized type.
     */
    fun <T : Any> codec(serializer: KSerializer<T>): SnapshotCodec<T> = object : SnapshotCodec<T> {
        override val schema = serializer.descriptor.fingerprint() * 31 + FORMAT_VERSION

        override fun write(writer: SnapshotWriter, value: T) = encode(serializer, value, writer)

        override fun read(reader: SnapshotReader): T = decode(serializer, reader)
    }
}

@OptIn(ExperimentalSerializationApi::class)
class SnapshotEncoder(private val writer: SnapshotWriter) :
    AbstractEncoder(),
    AnyValueEncoder {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun encodeBoolean(value: Boolean) = writer.boolean(value)

    override fun encodeByte(value: Byte) = writer.byte(value.toInt())

    override fun encodeShort(value: Short) = writer.short(value.toInt())

    override fun encodeChar(value: Char) = writer.int(value.code)

    override fun encodeInt(value: Int) = writer.int(value)

    override fun encodeLong(value: Long) = writer.long(value)

    override fun encodeFloat(value: Float) = writer.int(java.lang.Float.floatToRawIntBits(value))

    override fun encodeDouble(value: Double) = writer.double(value)

    override fun encodeString(value: String) = writer.string(value)

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = writer.int(index)

    override fun encodeNull() = writer.boolean(false)

    override fun encodeNotNullMark() = writer.boolean(true)

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        writer.int(collectionSize)
        return this
    }

    override fun encodeAnyValue(value: Any?) = writer.value(value)
}

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
