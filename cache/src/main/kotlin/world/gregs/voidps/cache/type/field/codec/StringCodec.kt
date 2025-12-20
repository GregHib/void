package world.gregs.voidps.cache.type.field.codec

import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Codec for non-nullable String values.
 */
object StringCodec : FieldCodec<String> {
    override fun bytes(value: String) = value.length + 1
    override fun readBinary(reader: Reader): String = reader.readString()
    override fun writeBinary(writer: Writer, value: String) = writer.writeString(value)
    override fun readConfig(reader: ConfigReader) = reader.string()
    override fun writeConfig(writer: ConfigWriter, value: String) = writer.writeValue(value)
}

/**
 * Codec for nullable String values.
 *
 * In config, the string "null" represents null.
 * In binary, null is represented as a 0-marker byte.
 */
object NullStringCodec : FieldCodec<String?> {
    override fun bytes(value: String?) = if (value == null) 1 else value.length + 1
    override fun readBinary(reader: Reader): String? {
        if (reader.peek() == 0) {
            reader.skip(1)
            return null
        }
        return reader.readString()
    }

    override fun writeBinary(writer: Writer, value: String?) = if (value == null) writer.writeByte(0) else writer.writeString(value)

    override fun readConfig(reader: ConfigReader): String? {
        val value = reader.string()
        if (value == "null") {
            return null
        }
        return value
    }

    override fun writeConfig(writer: ConfigWriter, value: String?) = writer.writeValue(value)
}
