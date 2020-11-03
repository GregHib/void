package rs.dusk.engine.entity.gfx.detail

import rs.dusk.cache.definition.decoder.GraphicDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class GraphicDetailsLoader(private val loader: FileLoader, private val decoder: GraphicDecoder) : TimedLoader<GraphicDetails>("graphic detail") {

    override fun load(args: Array<out Any?>): GraphicDetails {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return GraphicDetails(decoder, data, names)
    }
}