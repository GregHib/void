package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
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
