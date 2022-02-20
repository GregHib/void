package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.timedLoad

class EnumDefinitions(
    decoder: EnumDecoder
) : DefinitionsDecoded<EnumDefinition> {

    override val definitions: Array<EnumDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("enum definition", definitions.size, start)
    }

    override fun empty() = EnumDefinition.EMPTY

    fun load(): EnumDefinitions {
        timedLoad("enum extra") {
            ids = emptyMap()
            0
        }
        return this
    }

}