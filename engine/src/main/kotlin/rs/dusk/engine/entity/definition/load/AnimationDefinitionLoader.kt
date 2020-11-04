package rs.dusk.engine.entity.definition.load

import rs.dusk.cache.definition.decoder.AnimationDecoder
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.definition.AnimationDefinitions

class AnimationDefinitionLoader(private val loader: FileLoader, private val decoder: AnimationDecoder) : TimedLoader<AnimationDefinitions>("animation definition") {

    override fun load(args: Array<out Any?>): AnimationDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return AnimationDefinitions(decoder, data, names)
    }
}