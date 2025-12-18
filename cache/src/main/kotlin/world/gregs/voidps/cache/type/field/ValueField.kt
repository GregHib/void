package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeKey
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Base class for fields that store a single value of type T.
 */
class ValueField<T : Any?>(keys: List<String>, val default: T, internal val codec: FieldCodec<T>) : TypeField(keys) {

    constructor(key: String, default: T, codec: FieldCodec<T>) : this(listOf(key), default, codec)

    var value: T = default

    fun writeable(): Boolean = value != default

    override fun readBinary(reader: Reader, opcode: Int) {
        value = codec.readBinary(reader)
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        if (writeable()) {
            writer.writeByte(opcode)
            codec.writeBinary(writer, value)
            return true
        }
        return false
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        value = codec.readConfig(reader)
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        if (writeable()) {
            writer.writeKey(key)
            codec.writeConfig(writer, value)
            writer.write("\n")
        }
    }

    override fun reset() {
        value = default
    }

    fun writeBinary(writer: Writer) = codec.writeBinary(writer, value)
    fun writeConfig(writer: ConfigWriter) = codec.writeConfig(writer, value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueField<*>

        val thisValue = value
        val otherValue = other.value
        return when (thisValue) {
            is BooleanArray if otherValue is BooleanArray -> thisValue.contentEquals(otherValue)
            is ByteArray if otherValue is ByteArray -> thisValue.contentEquals(otherValue)
            is ShortArray if otherValue is ShortArray -> thisValue.contentEquals(otherValue)
            is IntArray if otherValue is IntArray -> thisValue.contentEquals(otherValue)
            is LongArray if otherValue is LongArray -> thisValue.contentEquals(otherValue)
            is DoubleArray if otherValue is DoubleArray -> thisValue.contentEquals(otherValue)
            is FloatArray if otherValue is FloatArray -> thisValue.contentEquals(otherValue)
            is CharArray if otherValue is CharArray -> thisValue.contentEquals(otherValue)
            is Array<*> if otherValue is Array<*> -> thisValue.contentEquals(otherValue)
            else -> value == other.value
        }
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString(): String {
        return when (val value = value) {
            is BooleanArray -> value.contentToString()
            is ByteArray -> value.contentToString()
            is ShortArray -> value.contentToString()
            is IntArray -> value.contentToString()
            is LongArray -> value.contentToString()
            is DoubleArray -> value.contentToString()
            is FloatArray -> value.contentToString()
            is CharArray -> value.contentToString()
            is Array<*> -> value.contentToString()
            else -> value.toString()
        }
    }

}