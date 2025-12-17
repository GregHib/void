package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable Int values.
 */
class IntField(
    key: String = "",
    default: Int = 0,
) : ValueField<Int>(key, default) {
    override fun readBinary(reader: Reader) = reader.readInt()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeInt(value)
    override fun readConfig(reader: ConfigReader): Int = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}

/**
 * Field for nullable Int values.
 *
 * In config, null is represented as the string "null".
 * In binary, null values throw an error as there is no valid way to represent null with integers.
 */
class NullIntField(
    key: String = "",
) : ValueField<Int?>(key, default = null) {
    override fun readBinary(reader: Reader) = reader.readInt()
    override fun writeBinary(writer: Writer, value: Int?) = writer.writeInt(value ?: throw IllegalArgumentException("Binary int fields cannot be null."))
    override fun readConfig(reader: ConfigReader) = if (reader.peek == '"') {
        check(reader.string() == "null")
        null
    } else {
        reader.int()
    }

    override fun writeConfig(writer: ConfigWriter, value: Int?) = writer.writeValue(value)
}