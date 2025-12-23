package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.PrimitiveField

class PairField<A, B>(
    val first: PrimitiveField<A>,
    val second: PrimitiveField<B>,
) : Field {
    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        first.readPacked(reader, index, opcode)
        second.readPacked(reader, index, opcode)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        if (first.default == first.get(index) && second.default == second.get(index)) {
            return
        }
        // We use codec directly to avoid writing the opcode twice
        writer.writeByte(opcode)
        first.codec.writeBinary(writer, first.get(index))
        second.codec.writeBinary(writer, second.get(index))
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        first.readConfig(reader, index, key)
        second.readConfig(reader, index, key)
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean {
        var written = false
        written = written or first.writeConfig(writer, index, key)
        written = written or second.writeConfig(writer, index, key)
        return written
    }

    override fun readDirect(reader: Reader) {
        first.readDirect(reader)
        second.readDirect(reader)
    }

    override fun writeDirect(writer: Writer) {
        first.writeDirect(writer)
        second.writeDirect(writer)
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as PairField<A, B>
        first.override(other.first, from, to)
        second.override(other.second, from, to)
    }

    override fun directSize(): Int = first.directSize() + second.directSize()

    override fun clear() {
        first.clear()
        second.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PairField<A, B>

        if (first != other.first) return false
        if (second != other.second) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        return result
    }

}