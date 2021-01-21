package world.gregs.void.cache.definition

import world.gregs.void.buffer.read.Reader

/**
 * @author GregHib <greg@gregs.world>
 * @since April 22, 2020
 */
interface ColourPalette {
    var recolourPalette: ByteArray?

    fun readColourPalette(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        recolourPalette = ByteArray(length)
        repeat(length) { count ->
            recolourPalette!![count] = buffer.readByte().toByte()
        }
    }
}