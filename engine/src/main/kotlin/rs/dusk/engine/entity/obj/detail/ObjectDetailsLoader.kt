package rs.dusk.engine.entity.obj.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class ObjectDetailsLoader(private val loader: FileLoader) : TimedLoader<ObjectDetails>("object detail") {

    override fun load(args: Array<out Any?>): ObjectDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, ObjectDetail> = data.mapValues { convert(it.value) }
        val objects = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return ObjectDetails(objects, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): ObjectDetail {
        val id: Int by map
        return ObjectDetail(id)
    }
}