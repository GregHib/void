package world.gregs.voidps.engine.entity.definition.load

import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions

class AnimationDefinitionLoader(private val loader: FileLoader, private val decoder: AnimationDecoder) : TimedLoader<AnimationDefinitions>("animation definition") {

    override fun load(args: Array<out Any?>): AnimationDefinitions {
        val path = args[0] as String
        val data: Map<String, Map<String, Any>> = loader.load(path)
        val names = data.map { it.value["id"] as Int to it.key }.toMap()
        count = names.size
        return AnimationDefinitions(decoder, data, names)
    }
}