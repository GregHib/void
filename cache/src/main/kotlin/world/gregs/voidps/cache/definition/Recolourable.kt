package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

interface Recolourable {
    var originalColours: ShortArray?
    var modifiedColours: ShortArray?
    var originalTextureColours: ShortArray?
    var modifiedTextureColours: ShortArray?

    fun readColours(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        originalColours = ShortArray(length)
        modifiedColours = ShortArray(length)
        for (count in 0 until length) {
            originalColours!![count] = buffer.readShort().toShort()
            modifiedColours!![count] = buffer.readShort().toShort()
        }
    }

    fun readTextures(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        originalTextureColours = ShortArray(length)
        modifiedTextureColours = ShortArray(length)
        for (count in 0 until length) {
            originalTextureColours!![count] = buffer.readShort().toShort()
            modifiedTextureColours!![count] = buffer.readShort().toShort()
        }
    }

    fun writeColoursTextures(writer: Writer) {
        writeArray(writer, 40, originalColours, modifiedColours)
        writeArray(writer, 41, originalTextureColours, modifiedTextureColours)
    }

    private fun writeArray(writer: Writer, opcode: Int, original: ShortArray?, modified: ShortArray?) {
        if (original != null && modified != null) {
            writer.writeByte(opcode)
            writer.writeByte(original.size)
            for (i in original.indices) {
                writer.writeShort(original[i].toInt())
                writer.writeShort(modified[i].toInt())
            }
        }
    }
}
