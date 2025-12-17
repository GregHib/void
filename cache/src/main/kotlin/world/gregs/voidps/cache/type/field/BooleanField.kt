package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable Boolean values.
 */
class BooleanField(
    key: String = "",
    default: Boolean = false,
) : ValueField<Boolean>(key, default) {
    override fun readBinary(reader: Reader) = reader.readBoolean()
    override fun writeBinary(writer: Writer, value: Boolean) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader): Boolean = reader.boolean()
    override fun writeConfig(writer: ConfigWriter, value: Boolean) = writer.writeValue(value)
}

/**
 * Field for nullable Boolean values.
 * In config, null is represented as the string "null".
 * In binary, null is represented as -1.
 */
class NullBooleanField(
    key: String = "",
) : ValueField<Boolean?>(key, default = null) {
    override fun readBinary(reader: Reader): Boolean? {
        if (reader.peek() == -1) {
            reader.skip(1)
            return null
        }
        return reader.readBoolean()
    }

    override fun writeBinary(writer: Writer, value: Boolean?) = if (value == null) {
        writer.writeByte(-1)
    } else {
        writer.writeByte(value)
    }

    override fun readConfig(reader: ConfigReader): Boolean? = if (reader.peek == '"') {
        check(reader.string() == "null")
        null
    } else {
        reader.boolean()
    }

    override fun writeConfig(writer: ConfigWriter, value: Boolean?) = writer.writeValue(value)
}