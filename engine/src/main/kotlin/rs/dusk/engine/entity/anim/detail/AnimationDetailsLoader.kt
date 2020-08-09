package rs.dusk.engine.entity.anim.detail

import com.google.common.collect.HashBiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader

class AnimationDetailsLoader(private val loader: FileLoader) : TimedLoader<AnimationDetails>("animation detail") {

    override fun load(args: Array<out Any?>): AnimationDetails {
        val path = args[0] as String
        val data: Map<String, LinkedHashMap<String, Any>> = loader.load(path)
        val map: Map<String, AnimationDetail> = data.mapValues { convert(it.value) }
        val animations = map.map { it.value.id to it.value }.toMap()
        val names = map.map { it.value.id to it.key }.toMap()
        count = names.size
        return AnimationDetails(animations, HashBiMap.create(names))
    }

    fun convert(map: Map<String, Any>): AnimationDetail {
        val id: Int by map
        return AnimationDetail(id)
    }
}