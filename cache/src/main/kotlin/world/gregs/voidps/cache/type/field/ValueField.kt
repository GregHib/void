package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writeKey
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeField

/**
 * Base class for fields that store a single value of type T.
 *
 * Provides default implementations for common field operations and defines
 * abstract methods for format-specific serialization.
 *
 * @param T The type of value this field stores (can be nullable)
 * @property default The default value, used when the field is not present in input
 */
abstract class ValueField<T : Any?>(
    keys: List<String>,
    val default: T,
) : TypeField(keys) {

    constructor(key: String, default: T) : this(listOf(key), default)

    internal var value: T = default

    @Suppress("UNCHECKED_CAST")
    override fun set(index: Int, value: Any?) {
        this.value = value as T
    }

    abstract fun readConfig(reader: ConfigReader): T
    abstract fun writeConfig(writer: ConfigWriter, value: T)

    abstract fun readBinary(reader: Reader): T
    abstract fun writeBinary(writer: Writer, value: T)

    override fun write(writer: Writer, opcode: Int): Boolean {
        if (value != default) {
            writer.writeByte(opcode)
            writeBinary(writer, value)
            return true
        }
        return false
    }

    override fun write(writer: ConfigWriter, key: String) {
        if (value != default) {
            writer.writeKey(key)
            writeConfig(writer, value)
            writer.write("\n")
        }
    }

    override fun read(reader: Reader, opcode: Int) {
        value = readBinary(reader)
    }

    override fun read(reader: ConfigReader, key: String) {
        value = readConfig(reader)
    }

    override fun reset() {
        value = default
    }
}