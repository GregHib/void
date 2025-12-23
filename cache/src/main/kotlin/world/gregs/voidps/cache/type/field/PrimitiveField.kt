package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeKey
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Base class for fields that store a single value of type T.
 */
interface PrimitiveField<T> : AccessibleField<T> {

    val codec: FieldCodec<T>
    val default: T

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        set(index, codec.readBinary(reader))
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        if (get(index) == default) {
            return
        }
        writer.writeByte(opcode)
        codec.writeBinary(writer, get(index))
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        set(index, codec.readConfig(reader))
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        if (get(index) == default) {
            return false
        }
        writer.writeKey(key)
        codec.writeConfig(writer, get(index))
        writer.write("\n")
        return true
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as PrimitiveField<T>
        if (other.get(from) == default) {
            return
        }
        set(to, other.get(from))
    }
}
