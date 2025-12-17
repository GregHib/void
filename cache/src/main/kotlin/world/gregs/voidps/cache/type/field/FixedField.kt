package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field which doesn't read or write any binary values but if found sets a [fixed] value.
 */
class FixedField<T: Any>(
    key: String = "",
    default: T,
    val fixed: T,
    val field: ValueField<T>
) : ValueField<T>(key, default) {
    override fun readBinary(reader: Reader) = fixed
    override fun writeBinary(writer: Writer, value: T) {}
    override fun readConfig(reader: ConfigReader): T = field.readConfig(reader)
    override fun writeConfig(writer: ConfigWriter, value: T) = writer.writeValue(value)
}