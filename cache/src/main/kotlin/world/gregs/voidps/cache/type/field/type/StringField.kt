package world.gregs.voidps.cache.type.field.type

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.PrimitiveField
import world.gregs.voidps.cache.type.field.codec.NullStringCodec
import world.gregs.voidps.cache.type.field.codec.StringCodec

class StringField(
    size: Int,
    override val default: String,
    override val codec: FieldCodec<String> = StringCodec
) : PrimitiveField<String> {

    val data = Array(size) { default }

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: String) {
        data[index] = value
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until data.size) {
            data[i] = codec.readBinary(reader)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (string in data) {
            codec.writeBinary(writer, string)
        }
    }

    override fun directSize(): Int = data.sumOf { codec.bytes(it) }

    override fun clear() = data.fill(default)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}

class NullStringField(
    size: Int,
    override val codec: FieldCodec<String?> = NullStringCodec
) : PrimitiveField<String?> {

    override val default: String? = null
    val data = arrayOfNulls<String?>(size)

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: String?) {
        data[index] = value
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until data.size) {
            data[i] = codec.readBinary(reader)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (string in data) {
            codec.writeBinary(writer, string)
        }
    }

    override fun directSize(): Int = data.size * 4

    override fun clear() = data.fill(default)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringField

        val otherValue = other.data
        return data.contentEquals(otherValue)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}