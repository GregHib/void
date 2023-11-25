package world.gregs.voidps.cache

import world.gregs.voidps.buffer.write.Writer

interface DefinitionEncoder<T : Definition> {
    open fun Writer.encode(definition: T, members: T) {
        encode(definition)
    }

    open fun Writer.encode(definition: T) {
        encode(definition, definition)
    }
}