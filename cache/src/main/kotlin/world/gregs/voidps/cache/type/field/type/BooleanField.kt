package world.gregs.voidps.cache.type.field.type

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.PrimitiveField
import world.gregs.voidps.cache.type.field.codec.BooleanCodec

/**
 * Base class for fields that store a single value of type T.
 */
class BooleanField(
    size: Int,
    override val default: Boolean,
    override val codec: FieldCodec<Boolean> = BooleanCodec,
) : PrimitiveField<Boolean> {
    // ByteArray faster to store and retrieve than BooleanArray
    val data = ByteArray(size) { if (default) 1 else 0 }

    override fun get(index: Int): Boolean = data[index] == 1.toByte()

    override fun set(index: Int, value: Boolean) {
        data[index] = if (value) 1 else 0
    }

    override fun readDirect(reader: Reader) = reader.readBytes(data)

    override fun writeDirect(writer: Writer) = writer.writeBytes(data)

    override fun directSize(): Int = data.size

    override fun clear() {
        data.fill(if (default) 1 else 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BooleanField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}