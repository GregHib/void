package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.TypeField
import world.gregs.voidps.cache.type.field.FieldCodec
import world.gregs.voidps.cache.type.field.codec.NullStringCodec

/**
 * Field for non-nullable string arrays with nullable elements: Array<T?>
 */
class IndexedStringArrayField(
    key: String,
    val default: Array<String?>,
    val offset: Int,
    val field: FieldCodec<String?> = NullStringCodec,
) : TypeField(listOf(key)) {
    var value: Array<String?> = default.clone()

    override fun readBinary(reader: Reader, opcode: Int) {
        value[opcode - offset] = field.readBinary(reader)
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        val value = value[opcode - offset]
        if (value == default[opcode - offset]) {
            return false
        }
        writer.writeByte(opcode)
        field.writeBinary(writer, value)
        return true
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        val list = mutableListOf<String?>()
        while (reader.nextElement()) {
            val value = reader.string()
            if (value == "null") {
                list.add(null)
            } else {
                list.add(value)
            }
        }
        value = list.toTypedArray()
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        if (value.contentEquals(default)) {
            return
        }
        writer.writePair(key, value)
    }

    override fun reset() {
        value = default.clone()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndexedStringArrayField

        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return value.contentToString()
    }

}