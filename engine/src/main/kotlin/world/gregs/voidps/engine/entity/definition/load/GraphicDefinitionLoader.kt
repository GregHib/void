package world.gregs.voidps.engine.entity.definition.load

import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions

class GraphicDefinitionLoader(private val loader: FileLoader, private val decoder: GraphicDecoder) : TimedLoader<GraphicDefinitions>("graphic definition") {

    override fun load(args: Array<out Any?>): GraphicDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return GraphicDefinitions(decoder, data, names)
    }
}