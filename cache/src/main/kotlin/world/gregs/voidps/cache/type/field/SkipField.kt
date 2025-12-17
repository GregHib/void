package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeField

/**
 * Read-only Field which skips reading values.
 */
class SkipField(
    val amount: (Reader) -> Int
) : TypeField(emptyList()) {
    override fun write(writer: Writer, opcode: Int): Boolean {
        return false
    }

    override fun read(reader: Reader, opcode: Int) {
        reader.skip(amount.invoke(reader))
    }

    override fun write(writer: ConfigWriter, key: String) {
    }

    override fun read(reader: ConfigReader, key: String) {
    }

    override fun set(index: Int, value: Any?) {
    }

    override fun reset() {
    }
}