package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

class GraphicDefinitions(
    override val decoder: GraphicDecoder
) : DefinitionsDecoder<GraphicDefinition, GraphicDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>

    fun load(loader: FileLoader = get(), path: String = getProperty("graphicDefinitionsPath")): GraphicDefinitions {
        timedLoad("graphic definition") {
            load(loader.load<Map<String, Map<String, Any>>>(path))
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>): Int {
        extras = data
        names = extras.map { it.value["id"] as Int to it.key }.toMap()
        return names.size
    }
}