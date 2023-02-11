package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

class ContainerDefinitions(
    decoder: ContainerDecoder
) : DefinitionsDecoder<ContainerDefinition> {

    override val definitions: Array<ContainerDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("container definition", definitions.size, start)
    }

    override fun empty() = ContainerDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("containerDefinitionsPath")): ContainerDefinitions {
        timedLoad("container extra") {
            decode(storage, path)
        }
        return this
    }
}

fun ContainerDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}