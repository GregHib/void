package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeField

/**
 * Field for non-nullable string arrays with nullable elements: Array<T?>
 */
class IndexedStringArrayField(
    key: String,
    val default: Array<String?>,
    val field: ValueField<String?>,
    val offset: Int,
) : TypeField(listOf(key)) {
    var array: Array<String?> = default

    override fun write(writer: Writer, opcode: Int): Boolean {
        val value = array[opcode - offset]
        if (value == default[opcode - offset]) {
            return false
        }
        field.writeBinary(writer, value)
        return true
    }

    override fun read(reader: Reader, opcode: Int) {
        array[opcode - offset] = field.readBinary(reader)
    }

    override fun write(writer: ConfigWriter, key: String) {
        if (array.contentEquals(default)) {
            return
        }
        writer.writePair(key, array)
    }

    override fun read(reader: ConfigReader, key: String) {
        val list = mutableListOf<String?>()
        while (reader.nextElement()) {
            val value = reader.string()
            if (value == "null") {
                list.add(null)
            } else {
                list.add(value)
            }
        }
        array = list.toTypedArray()
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(index: Int, value: Any?) {
        array = value as Array<String?>
    }

    override fun reset() {
        array = default
    }
}
