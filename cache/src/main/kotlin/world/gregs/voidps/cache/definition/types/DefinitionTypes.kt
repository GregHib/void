package world.gregs.voidps.cache.definition.types

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.data.NPCDefinition

interface DefinitionTypes<T : Definition> : Types {
    val loaded: ByteArray

    fun load(id: Int, definition: T)
    fun save(id: Int, definition: T)

    fun load(definitions: Array<T>) {
        for (id in definitions.indices) {
            val definition = definitions[id]
            if (definition == NPCDefinition.EMPTY) {
                loaded[id] = 0
                continue
            }
            loaded[id] = 1
            load(id, definition)
        }
    }

    fun save(definitions: Array<T>) {
        for (id in definitions.indices) {
            if (loaded[id] == 0.toByte()) {
                continue
            }
            save(id, definitions[id])
        }
    }
}