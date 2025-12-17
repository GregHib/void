package world.gregs.voidps.cache.type

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
     * Writes this field to binary format if its value differs from default.
     * @return true if the field was written, false if skipped (matches default)
     */
    abstract fun write(writer: Writer, opcode: Int): Boolean

    /**
     * Reads this field's value from binary format.
     */
    abstract fun read(reader: Reader, opcode: Int)

    /**
     * Writes this field to Config format if its value differs from default.
     */
    abstract fun write(writer: ConfigWriter, key: String)

    /**
     * Reads this field's value from Config format for a specific key.
     */
    abstract fun read(reader: ConfigReader, key: String)

    /**
     * Sets this field's value from a Type's component at the given index.
     * Used during serialization to extract values from the Type instance.
     * @see [Type.component]
     */
    abstract fun set(index: Int, value: Any?)

    /**
     * Resets this field to its default value.
     */
    abstract fun reset()
}