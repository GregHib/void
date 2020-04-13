package org.redrune.cache.definition

import org.redrune.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
interface Recolourable {
    var originalColours: ShortArray?
    var modifiedColours: ShortArray?
    var originalTextureColours: ShortArray?
    var modifiedTextureColours: ShortArray?

    fun readColours(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        originalColours = ShortArray(length)
        modifiedColours = ShortArray(length)
        repeat(length) { count ->
            originalColours!![count] = buffer.readShort().toShort()
            modifiedColours!![count] = buffer.readShort().toShort()
        }
    }

    fun readTextures(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        originalTextureColours = ShortArray(length)
        modifiedTextureColours = ShortArray(length)
        repeat(length) { count ->
            originalTextureColours!![count] = buffer.readShort().toShort()
            modifiedTextureColours!![count] = buffer.readShort().toShort()
        }
    }
}