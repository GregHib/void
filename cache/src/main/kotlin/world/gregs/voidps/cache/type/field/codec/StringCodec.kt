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
    private val buffer = ByteArray(256)

    /**
     * Faster impl of [Reader.readString] assuming string doesn't exceed 256 bytes.
     */
    override fun readBinary(reader: Reader): String {
        var index = 0
        var b: Int
        while (reader.remaining > 0) {
            b = reader.readUnsignedByte()
            if (b == 0) {
                break
            }
            buffer[index++] = b.toByte()
        }
        return String(buffer, 0, index)
    }
    override fun writeBinary(writer: Writer, value: String) = writer.writeString(value)
    override fun readConfig(reader: ConfigReader) = reader.string()
    override fun writeConfig(writer: ConfigWriter, value: String) = writer.writeValue(value)
}

/**
 * Codec for nullable String values.
 *
 * In config, the string "null" represents null.
 * In binary, null is represented as -1 marker byte.
 */
object NullStringCodec : FieldCodec<String?> {
    override fun readBinary(reader: Reader): String? {
        if (reader.peek() == -1) {
            println("Found null string")
            reader.skip(1)
            return null
        }
        return reader.readString()
    }
    override fun writeBinary(writer: Writer, value: String?) = if (value == null) {
        println("Write null string")
        writer.writeByte(-1)
    } else writer.writeString(value)
    override fun readConfig(reader: ConfigReader): String? {
        val value = reader.string()
        if (value == "null") {
            return null
        }
        return value
    }
    override fun writeConfig(writer: ConfigWriter, value: String?) = writer.writeValue(value)
}
