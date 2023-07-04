package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.StructDefinition

class StructDefinitions(
    override var definitions: Array<StructDefinition>
) : DefinitionsDecoder<StructDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = StructDefinition.EMPTY

    fun load(): StructDefinitions {
        return this
    }

}