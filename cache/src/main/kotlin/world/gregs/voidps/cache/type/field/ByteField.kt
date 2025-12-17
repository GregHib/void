package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable byte values.
 */
class ByteField(
    key: String = "",
    default: Byte = 0,
) : ValueField<Byte>(key, default) {
    override fun readBinary(reader: Reader) = reader.readByte().toByte()
    override fun writeBinary(writer: Writer, value: Byte) = writer.writeByte(value.toInt())
    override fun readConfig(reader: ConfigReader): Byte = reader.int().toByte()
    override fun writeConfig(writer: ConfigWriter, value: Byte) = writer.writeValue(value)
}

/**
 * Field for non-nullable int values stored as a byte.
 */
class ByteIntField(
    key: String = "",
    default: Int = 0,
) : ValueField<Int>(key, default) {
    override fun readBinary(reader: Reader) = reader.readByte()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeByte(value)
    override fun readConfig(reader: ConfigReader): Int = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}
