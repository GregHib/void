package rs.dusk.engine.entity.character.npc.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class NPCDetailsLoader(private val loader: FileLoader) : TimedLoader<NPCDetails>("npc detail") {

    override fun load(args: Array<out Any?>): NPCDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, NPCDetail> = data.mapValues { convert(it.value) }
        val npcs = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return NPCDetails(npcs, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): NPCDetail {
        val id: Int by map
        return NPCDetail(id)
    }
}