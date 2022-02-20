package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.data.MiningRock
import world.gregs.voidps.engine.entity.definition.data.Tree
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ObjectDefinitions(
    decoder: ObjectDecoder
) : DefinitionsDecoded<ObjectDefinition> {
    override val definitions: Array<ObjectDefinition?>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("object definition", definitions.size, start)
    }

    override fun empty() = ObjectDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("objectDefinitionsPath")): ObjectDefinitions {
        timedLoad("object extra") {
            val data = storage.loadMapIds(path)
            val modifications = DefinitionModifications()
            modifications.map("woodcutting") { Tree(it) }
            modifications.map("mining") { MiningRock(it) }
            val extras = modifications.apply(data)
            val names = extras.map { it.value["id"] as Int to it.key }.toMap()
            ids = extras.map { it.key to it.value["id"] as Int }.toMap()
            apply(names, extras)
            names.size
        }
        return this
    }
}

@Suppress("UNCHECKED_CAST")
internal fun FileStorage.loadMapIds(path: String): Map<String, Map<String, Any>> = load<Map<String, Any>>(path).mapValues { (_, value) ->
    if (value is Int) mapOf("id" to value) else value as Map<String, Any>
}