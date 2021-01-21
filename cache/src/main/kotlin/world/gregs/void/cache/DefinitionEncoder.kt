package world.gregs.void.cache

import world.gregs.void.buffer.write.Writer

/**
 * @author GregHib <greg@gregs.world>
 * @since April 13, 2020
 */
interface DefinitionEncoder<T : Definition> {
    fun Writer.encode(definition: T)
}