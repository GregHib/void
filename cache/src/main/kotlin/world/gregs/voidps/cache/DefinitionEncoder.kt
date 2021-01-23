package world.gregs.voidps.cache

import world.gregs.voidps.buffer.write.Writer

/**
 * @author GregHib <greg@gregs.world>
 * @since April 13, 2020
 */
interface DefinitionEncoder<T : Definition> {
    fun Writer.encode(definition: T)
}