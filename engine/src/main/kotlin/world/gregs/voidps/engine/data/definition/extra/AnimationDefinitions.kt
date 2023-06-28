package world.gregs.voidps.engine.data.definition.extra

import world.gregs.voidps.cache.definition.data.AnimationDefinition
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

class AnimationDefinitions(
    decoder: AnimationDecoder
) : DefinitionsDecoder<AnimationDefinition> {

    override lateinit var definitions: Array<AnimationDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map { decoder.get(it) }.toTypedArray()
        timedLoad("animation definition", definitions.size, start)
    }

    override fun empty() = AnimationDefinition.EMPTY

    fun load(yaml: Yaml = get(), path: String = getProperty("animationDefinitionsPath")): AnimationDefinitions {
        timedLoad("animation extra") {
            decode(yaml, path)
        }
        return this
    }

}