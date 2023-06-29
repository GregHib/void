package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.timedLoad

class StructDefinitions(
    decoder: StructDecoder
) : DefinitionsDecoder<StructDefinition> {

    override lateinit var definitions: Array<StructDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("struct definition", definitions.size, start)
    }

    override fun empty() = StructDefinition.EMPTY

    fun load(): StructDefinitions {
        return this
    }

}