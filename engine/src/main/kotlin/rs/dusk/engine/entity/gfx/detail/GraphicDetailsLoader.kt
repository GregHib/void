package rs.dusk.engine.entity.gfx.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class GraphicDetailsLoader(private val loader: FileLoader) : TimedLoader<GraphicDetails>("graphic detail") {

    override fun load(args: Array<out Any?>): GraphicDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, GraphicDetail> = data.mapValues { convert(it.value) }
        val graphics = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return GraphicDetails(graphics, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): GraphicDetail {
        val id: Int by map
        return GraphicDetail(id)
    }
}