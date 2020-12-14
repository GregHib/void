package rs.dusk.cache

import rs.dusk.buffer.write.Writer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 13, 2020
 */
interface DefinitionEncoder<T : Definition> {
    fun Writer.encode(definition: T)
}