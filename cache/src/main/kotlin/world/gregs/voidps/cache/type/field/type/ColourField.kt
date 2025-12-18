package world.gregs.voidps.cache.type.field.type

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.config.writeKey
import world.gregs.config.writeValue
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.field.TypeField

class ColourField(
    val originalKey: String,
    val modifiedKey: String,
) : TypeField(listOf(originalKey, modifiedKey)) {
    var original: ShortArray? = null
    var modified: ShortArray? = null

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        val original = original
        val modified = modified
        if (original == null || modified == null) {
            return false
        }
        writer.writeByte(opcode)
        writer.writeByte(original.size)
        for (i in original.indices) {
            writer.writeShort(original[i].toInt())
            writer.writeShort(modified[i].toInt())
        }
        return true
    }

    override fun readBinary(reader: Reader, opcode: Int) {
        val size = reader.readUnsignedByte()
        original = ShortArray(size)
        modified = ShortArray(size)
        for (count in 0 until size) {
            original!![count] = reader.readShort().toShort()
            modified!![count] = reader.readShort().toShort()
        }
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        when (key) {
            originalKey -> original = readArray(reader)
            modifiedKey -> modified = readArray(reader)
        }
    }

    private fun readArray(reader: ConfigReader): ShortArray {
        val list = mutableListOf<Short>()
        while (reader.nextElement()) {
            list.add(reader.int().toShort())
        }
        return ShortArray(list.size) { list[it] }
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        when (key) {
            originalKey -> writeArray(writer, key, original ?: return)
            modifiedKey -> writeArray(writer, key, modified ?: return)
        }
    }

    private fun writeArray(writer: ConfigWriter, key: String, array: ShortArray) {
        writer.writeKey(key)
        writer.list(array.size) {
            writeValue(array[it].toInt())
        }
        writer.write("\n")
    }

    override fun reset() {
        original = null
        modified = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColourField

        if (!original.contentEquals(other.original)) return false
        if (!modified.contentEquals(other.modified)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = original?.contentHashCode() ?: 0
        result = 31 * result + (modified?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ColourField(original=${original.contentToString()}, modified=${modified.contentToString()})"
    }

}