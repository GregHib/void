package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.cache.serialization.AnyValueEncoder

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
