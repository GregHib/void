package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.Field
import world.gregs.voidps.cache.type.field.PrimitiveField

class TripleField<A, B, C>(
    val first: PrimitiveField<A>,
    val second: PrimitiveField<B>,
    val third: PrimitiveField<C>,
) : Field {

    override fun readPacked(reader: Reader, index: Int, opcode: Int) {
        first.readPacked(reader, index, opcode)
        second.readPacked(reader, index, opcode)
        third.readPacked(reader, index, opcode)
    }

    override fun writePacked(writer: Writer, index: Int, opcode: Int) {
        if (first.default == first.get(index) && second.default == second.get(index) && third.default == third.get(index)) {
            return
        }
        writer.writeByte(opcode)
        first.codec.writeBinary(writer, first.get(index))
        second.codec.writeBinary(writer, second.get(index))
        third.codec.writeBinary(writer, third.get(index))
    }

    override fun readConfig(reader: ConfigReader, index: Int, key: String) {
        first.readConfig(reader, index, key)
        second.readConfig(reader, index, key)
        third.readConfig(reader, index, key)
    }

    override fun writeConfig(writer: ConfigWriter, index: Int, key: String) {
        first.writeConfig(writer, index, key)
        second.writeConfig(writer, index, key)
        third.writeConfig(writer, index, key)
    }

    override fun readDirect(reader: Reader) {
        first.readDirect(reader)
        second.readDirect(reader)
        third.readDirect(reader)
    }

    override fun writeDirect(writer: Writer) {
        first.writeDirect(writer)
        second.writeDirect(writer)
        third.writeDirect(writer)
    }

    override fun override(other: Field, from: Int, to: Int) {
        other as TripleField<A, B, C>
        first.override(other.first, from, to)
        second.override(other.second, from, to)
        third.override(other.third, from, to)
    }

    override fun directSize(): Int = first.directSize() + second.directSize() + third.directSize()

    override fun clear() {
        first.clear()
        second.clear()
        third.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TripleField<A, B, C>

        if (first != other.first) return false
        if (second != other.second) return false
        if (third != other.third) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        result = 31 * result + third.hashCode()
        return result
    }

}