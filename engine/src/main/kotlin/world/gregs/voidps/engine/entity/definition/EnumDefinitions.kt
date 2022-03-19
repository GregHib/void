package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.getProperty

class EnumDefinitions(
    decoder: EnumDecoder
) : DefinitionsDecoder<EnumDefinition> {

    override val definitions: Array<EnumDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("enum definition", definitions.size, start)
    }

    override fun empty() = EnumDefinition.EMPTY

    fun load(storage: FileStorage = world.gregs.voidps.engine.utility.get(), path: String = getProperty("enumDefinitionsPath")): EnumDefinitions {
        timedLoad("enum extra") {
            decode(storage, path)
        }
        return this
    }

}