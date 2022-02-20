package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class GraphicDefinitions(
    decoder: GraphicDecoder
) : DefinitionsDecoded<GraphicDefinition> {

    override val definitions: Array<GraphicDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("graphic definition", definitions.size, start)
    }

    override fun empty() = GraphicDefinition.EMPTY

    fun load(storage: FileStorage = get(), path: String = getProperty("graphicDefinitionsPath")): GraphicDefinitions {
        timedLoad("graphic extra") {
            val data = storage.loadMapIds(path)
            val names = data.map { it.value["id"] as Int to it.key }.toMap()
            ids = data.map { it.key to it.value["id"] as Int }.toMap()
            apply(names, data)
            names.size
        }
        return this
    }

}