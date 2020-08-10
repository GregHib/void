package rs.dusk.engine.entity.character.contain.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.character.contain.StackMode

class ContainerDetailsLoader(private val loader: FileLoader) : TimedLoader<ContainerDetails>("container detail") {

    override fun load(args: Array<out Any?>): ContainerDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, ContainerDetail> = data.mapValues { convert(it.value) }
        val containers = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return ContainerDetails(containers, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): ContainerDetail {
        val id: Int by map
        val stack = map["stack"] as? String ?: "Normal"
        val mode = StackMode.valueOf(stack)
        return ContainerDetail(id, mode)
    }
}