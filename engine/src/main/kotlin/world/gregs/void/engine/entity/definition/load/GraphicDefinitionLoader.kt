package world.gregs.void.engine.entity.definition.load

import world.gregs.void.cache.definition.decoder.GraphicDecoder
import world.gregs.void.engine.TimedLoader
import world.gregs.void.engine.data.file.FileLoader
import world.gregs.void.engine.entity.definition.GraphicDefinitions

class GraphicDefinitionLoader(private val loader: FileLoader, private val decoder: GraphicDecoder) : TimedLoader<GraphicDefinitions>("graphic definition") {

    override fun load(args: Array<out Any?>): GraphicDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return GraphicDefinitions(decoder, data, names)
    }
}