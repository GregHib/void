package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

/**
 * Base class for all field types.
 *
 * A field handles the serialization and deserialization of a single property
 * in both binary and [world.gregs.config.Config] formats.
 *
 * @property keys The Config keys this field responds to (usually one, but can be multiple)
 */
abstract class TypeField(
    val keys: List<String>,
) {

    /**
     * Overrides this field's value with the value from [other].
     */
    abstract fun join(other: TypeField)

    /**
     * Reads this field's value from binary format.
     */
    abstract fun readBinary(reader: Reader, opcode: Int)

    /**
     * Writes this field to binary format if its value differs from default.
     * @return true if the field was written, false if skipped (matches default)
     */
    abstract fun writeBinary(writer: Writer, opcode: Int): Boolean

    /**
     * Reads this field's value from Config format for a specific key.
     */
    abstract fun readConfig(reader: ConfigReader, key: String)

    /**
     * Writes this field to Config format if its value differs from default.
     */
    abstract fun writeConfig(writer: ConfigWriter, key: String)

    /**
     * Resets this field to its default value.
     * Important that reset changes the value not just clears it as that will modify other references.
     */
    abstract fun reset()
}