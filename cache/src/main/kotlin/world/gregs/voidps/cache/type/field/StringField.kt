package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable String values.
 */
class StringField(
    key: String = "",
    default: String = "",
) : ValueField<String>(key, default) {
    override fun readBinary(reader: Reader) = reader.readString()
    override fun writeBinary(writer: Writer, value: String) = writer.writeString(value)
    override fun readConfig(reader: ConfigReader) = reader.string()
    override fun writeConfig(writer: ConfigWriter, value: String) = writer.writeValue(value)
}

/**
 * Field for nullable String values.
 *
 * In config, the string "null" represents null.
 * In binary, null is represented as -1 marker byte.
 */
class NullStringField(
    key: String = "",
) : ValueField<String?>(key, default = null) {
    override fun readBinary(reader: Reader): String? {
        if (reader.peek() == -1) {
            reader.skip(1)
            return null
        }
        return reader.readString()
    }

    override fun writeBinary(writer: Writer, value: String?) = if (value == null) {
        writer.writeByte(-1)
    } else {
        writer.writeString(value)
    }

    override fun readConfig(reader: ConfigReader): String? {
        val value = reader.string()
        if (value == "null") {
            return null
        }
        return value
    }

    override fun writeConfig(writer: ConfigWriter, value: String?) = writer.writeValue(value)
}