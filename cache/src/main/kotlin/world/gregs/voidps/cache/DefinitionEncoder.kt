package world.gregs.voidps.cache

import world.gregs.voidps.buffer.write.Writer

interface DefinitionEncoder<T : Definition> {
    fun Writer.encode(definition: T)
}