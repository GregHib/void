package world.gregs.voidps.cache.type.field

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.type.StringField

/**
 * Base class for all field types.
 *
 * A field handles the serialization and deserialization of a single property
 * in both binary and [world.gregs.config.Config] formats.
 */
open class ValueField<T : Any>(
    override val default: T,
    override val codec: FieldCodec<T>,
    create: () -> Array<T>,
) : PrimitiveField<T> {

    val data = create()

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: T) {
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

        other as ValueField<T>

        for (i in data.indices) {
            if (data[i] is ByteArray && other.data[i] is ByteArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is ShortArray && other.data[i] is ShortArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is IntArray && other.data[i] is IntArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is Array<*> && other.data[i] is Array<*>) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is List<*> && other.data[i] is List<*>) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] != other.data[i]) {
                println("Dif ${data[i]}")
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}

class NullValueField<T : Any>(
    override val codec: FieldCodec<T?>,
    create: () -> Array<T?>,
) : PrimitiveField<T?> {

    override val default: T? = null
    val data = create()

    override fun get(index: Int) = data[index]

    override fun set(index: Int, value: T?) {
        data[index] = value
    }

    override fun readDirect(reader: Reader) {
        for (i in 0 until data.size) {
            data[i] = codec.readBinary(reader)
        }
    }

    override fun writeDirect(writer: Writer) {
        for (value in data) {
            codec.writeBinary(writer, value)
        }
    }

    override fun directSize(): Int = data.sumOf { codec.bytes(it) }

    override fun clear() = data.fill(default)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NullValueField<T>

        for (i in data.indices) {
            if (data[i] is ByteArray && other.data[i] is ByteArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is ShortArray && other.data[i] is ShortArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is IntArray && other.data[i] is IntArray) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is Array<*> && other.data[i] is Array<*>) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] is List<*> && other.data[i] is List<*>) {
                return data.contentDeepEquals(other.data)
            } else if (data[i] != other.data[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString() = data.contentToString()

}