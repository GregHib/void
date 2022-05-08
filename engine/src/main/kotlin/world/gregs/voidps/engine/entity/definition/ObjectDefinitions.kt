package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.data.Rock
import world.gregs.voidps.engine.entity.definition.data.Tree
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ObjectDefinitions(
    decoder: ObjectDecoder
) : DefinitionsDecoder<ObjectDefinition> {

    override val definitions: Array<ObjectDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("object definition", definitions.size, start)
    }

    override fun empty() = ObjectDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("objectDefinitionsPath"), itemDefinitions: ItemDefinitions? = get()): ObjectDefinitions {
        timedLoad("object extra") {
            val modifications = DefinitionModifications()
            if (itemDefinitions != null) {
                modifications.map("woodcutting") { Tree(it, itemDefinitions) }
                modifications.map("mining") { Rock(it, itemDefinitions) }
            }
            decode(storage, path, modifications)
        }
        return this
    }
}