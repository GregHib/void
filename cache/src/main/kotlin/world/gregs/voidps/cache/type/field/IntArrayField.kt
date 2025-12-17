package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Field for nullable integer arrays.
 * Null and empty arrays are both serialized as length 0 in binary.
 */
class IntArrayField(
    key: String = "",
    default: IntArray? = null,
    val field: IntField,
) : ValueField<IntArray?>(key, default) {
    override fun readBinary(reader: Reader): IntArray? {
        val size = reader.readByte()
        if (size == 0 && default == null) {
            return null
        }
        return IntArray(size) { field.readBinary(reader) }
    }

    override fun writeBinary(writer: Writer, value: IntArray?) {
        if (value == null || value.isEmpty()) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(value.size)
        for (v in value) {
            field.writeBinary(writer, v)
        }
    }

    override fun readConfig(reader: ConfigReader): IntArray {
        val list = mutableListOf<Int>()
        while (reader.nextElement()) {
            list.add(field.readConfig(reader))
        }
        return list.toIntArray()
    }

    override fun writeConfig(writer: ConfigWriter, value: IntArray?) {
        writer.writeValue(value)
    }
}