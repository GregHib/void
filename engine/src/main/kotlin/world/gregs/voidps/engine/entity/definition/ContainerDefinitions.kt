package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

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
            val modifications = DefinitionModifications()
            modifications["stack"] = { StackMode.valueOf(it as String) }
            decode(storage, path, modifications)
        }
        return this
    }
}

fun ContainerDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}