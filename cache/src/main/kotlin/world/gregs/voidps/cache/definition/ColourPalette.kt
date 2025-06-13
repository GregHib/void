package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

interface ColourPalette {
    var recolourPalette: ByteArray?

    fun readColourPalette(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        recolourPalette = ByteArray(length) { buffer.readByte().toByte() }
    }

    fun writeRecolourPalette(writer: Writer) {
        val palette = recolourPalette
        if (palette != null) {
            writer.writeByte(42)
            writer.writeByte(palette.size)
            for (colour in palette) {
                writer.writeByte(colour.toInt())
            }
        }
    }
}
