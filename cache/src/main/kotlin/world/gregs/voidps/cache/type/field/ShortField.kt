package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for non-nullable short values.
 */
class ShortField(
    key: String = "",
    default: Short = 0,
) : ValueField<Short>(key, default) {
    override fun readBinary(reader: Reader) = reader.readShort().toShort()
    override fun writeBinary(writer: Writer, value: Short) = writer.writeShort(value.toInt())
    override fun readConfig(reader: ConfigReader): Short = reader.int().toShort()
    override fun writeConfig(writer: ConfigWriter, value: Short) = writer.writeValue(value)
}

/**
 * Field for non-nullable int values stored as a short.
 */
class ShortIntField(
    key: String = "",
    default: Int = 0,
) : ValueField<Int>(key, default) {
    override fun readBinary(reader: Reader) = reader.readShort()
    override fun writeBinary(writer: Writer, value: Int) = writer.writeShort(value)
    override fun readConfig(reader: ConfigReader): Int = reader.int()
    override fun writeConfig(writer: ConfigWriter, value: Int) = writer.writeValue(value)
}
