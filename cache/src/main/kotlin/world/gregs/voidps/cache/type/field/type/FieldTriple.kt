package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.TypeField
import world.gregs.voidps.cache.type.field.ValueField

data class FieldTriple<A, B, C>(
    val first: ValueField<A>,
    val second: ValueField<B>,
    val third: ValueField<C>,
) : TypeField(first.keys + second.keys + third.keys) {

    override fun join(other: TypeField) {
        other as FieldTriple<A, B, C>
        first.join(other.first)
        second.join(other.second)
        third.join(other.third)
    }

    override fun readBinary(reader: Reader, opcode: Int) {
        first.readBinary(reader, opcode)
        second.readBinary(reader, opcode)
        third.readBinary(reader, opcode)
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        if (!first.different() || !second.different() || !third.different()) {
            return false
        }
        writer.writeByte(opcode)
        first.writeBinary(writer)
        second.writeBinary(writer)
        third.writeBinary(writer)
        return true
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        if (first.keys.contains(key)) {
            first.readConfig(reader, key)
        } else if (second.keys.contains(key)) {
            second.readConfig(reader, key)
        } else if (third.keys.contains(key)) {
            third.readConfig(reader, key)
        }
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        if (first.keys.contains(key) && first.different()) {
            first.writeConfig(writer, key)
        } else if (second.keys.contains(key) && second.different()) {
            second.writeConfig(writer, key)
        } else if (third.keys.contains(key) && third.different()) {
            third.writeConfig(writer, key)
        }
    }

    override fun reset() {
        first.reset()
        second.reset()
        third.reset()
    }
}