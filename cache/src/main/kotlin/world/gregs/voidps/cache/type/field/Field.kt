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
interface Field {

    /**
     * Reads [index] field's value from a packed binary format.
     */
    fun readPacked(reader: Reader, index: Int, opcode: Int)

    /**
     * Writes [index] field to packed binary format if its value differs from default.
     * @return true if the field was written, false if skipped (matches default)
     */
    fun writePacked(writer: Writer, index: Int, opcode: Int): Boolean

    /**
     * Reads [index] field's value from Config format for a specific key.
     */
    fun readConfig(reader: ConfigReader, index: Int, key: String)

    /**
     * Writes [index] field to Config format if its value differs from default.
     */
    fun writeConfig(writer: ConfigWriter, index: Int, key: String): Boolean

    /**
     * Reads all field's value in a fast binary format.
     */
    fun readDirect(reader: Reader)

    /**
     * Writes all field's value to a fast binary format.
     */
    fun writeDirect(writer: Writer)

    /**
     * Precomputed size of all the data produced by [writeDirect].
     */
    fun directSize(): Int

    /**
     * Overrides the value at [to] with the [from] fields value if [other] is not default.
     */
    fun override(other: Field, from: Int, to: Int = from)

    /**
     * Clears all fields to their default value.
     * Important that reset changes the value not just clears it as that will modify other references.
     */
    fun clear()
}