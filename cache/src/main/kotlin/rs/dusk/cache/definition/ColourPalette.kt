package rs.dusk.cache.definition

import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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