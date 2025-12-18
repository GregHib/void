package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.TypeField
import world.gregs.voidps.cache.type.field.ValueField

data class FieldPair<A, B>(
    val first: ValueField<A>,
    val second: ValueField<B>,
) : TypeField(first.keys + second.keys) {
    override fun readBinary(reader: Reader, opcode: Int) {
        first.readBinary(reader, opcode)
        second.readBinary(reader, opcode)
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        if (!first.writeable() || !second.writeable()) {
            return false
        }
        writer.writeByte(opcode)
        first.writeBinary(writer)
        second.writeBinary(writer)
        return true
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        if (first.keys.contains(key)) {
            first.readConfig(reader, key)
        } else if (second.keys.contains(key)) {
            second.readConfig(reader, key)
        }
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        if (first.keys.contains(key) && first.writeable()) {
            first.writeConfig(writer, key)
        } else if (second.keys.contains(key) && second.writeable()) {
            second.writeConfig(writer, key)
        }
    }

    override fun reset() {
        first.reset()
        second.reset()
    }

}
