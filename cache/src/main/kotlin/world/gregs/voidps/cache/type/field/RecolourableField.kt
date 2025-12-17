package world.gregs.voidps.cache.type.field

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.list
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeField

/**
 * Field for storing paired colour arrays for texture recolouring.
 */
class RecolourableField(val originalKey: String, val modifiedKey: String) : TypeField(listOf(originalKey, modifiedKey)) {

    var originalColours: ShortArray? = null
    var modifiedColours: ShortArray? = null

    override fun write(writer: Writer, opcode: Int): Boolean {
        val originalColours = originalColours
        val modifiedColours = modifiedColours
        if (originalColours != null && modifiedColours != null) {
            writer.writeByte(opcode)
            writer.writeByte(originalColours.size)
            for (i in originalColours.indices) {
                writer.writeShort(originalColours[i].toInt())
                writer.writeShort(modifiedColours[i].toInt())
            }
            return true
        }
        return false
    }

    override fun read(reader: Reader, opcode: Int) {
        val size = reader.readUnsignedByte()
        originalColours = ShortArray(size)
        modifiedColours = ShortArray(size)
        for (count in 0 until size) {
            originalColours!![count] = reader.readShort().toShort()
            modifiedColours!![count] = reader.readShort().toShort()
        }
    }

    override fun read(reader: ConfigReader, key: String) {
        when (key) {
            originalKey -> {
                val list = mutableListOf<Short>()
                while (reader.nextElement()) {
                    list.add(reader.int().toShort())
                }
                originalColours = ShortArray(list.size) { list[it] }
            }
            modifiedKey -> {
                val list = mutableListOf<Short>()
                while (reader.nextElement()) {
                    list.add(reader.int().toShort())
                }
                modifiedColours = ShortArray(list.size) { list[it] }
            }
        }
    }

    override fun write(writer: ConfigWriter, key: String) {
        when (key) {
            originalKey -> {
                val original = originalColours ?: return
                writer.list(original.size) {
                    write(original[it].toInt())
                }
            }
            modifiedKey -> {
                val modified = modifiedColours ?: return
                writer.list(modified.size) {
                    write(modified[it].toInt())
                }
            }
        }
    }

    override fun reset() {
        originalColours = null
        modifiedColours = null
    }

    override fun set(index: Int, value: Any?) {
        when (index) {
            1 -> originalColours = value as ShortArray?
            2 -> modifiedColours = value as ShortArray?
        }
    }
}
